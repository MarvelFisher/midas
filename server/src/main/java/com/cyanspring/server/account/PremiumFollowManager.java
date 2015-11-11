package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PremiumFollowInfo;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.PremiumFollowGlobalReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowGlobalRequestEvent;
import com.cyanspring.common.event.account.PremiumFollowPositionGlobalReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowPositionGlobalRequestEvent;
import com.cyanspring.common.event.account.PremiumFollowPositionReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowPositionRequestEvent;
import com.cyanspring.common.event.account.PremiumFollowReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowRequestEvent;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageBean;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.position.PositionKeeper;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.event.AsyncEventProcessor;

public class PremiumFollowManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(PremiumFollowManager.class);

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	@Qualifier("globalEventManager")
	private IRemoteEventManager globalEventManager;
	
	@Autowired
	private PositionKeeper positionKeeper;
	
	@Autowired
	private AccountKeeper accountKeeper;
	
	private Map<String, PremiumFollowGlobalRequestEvent> pendingRequests = 
				new ConcurrentHashMap<String, PremiumFollowGlobalRequestEvent>();
	private Map<String, Tx> txIdMap = new HashMap<String, Tx>();
	private ScheduleManager scheduleManager = new ScheduleManager();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long timerInterval = 5000;
	private long timeout = 10000;
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PremiumFollowRequestEvent.class, null);
			subscribeToEvent(PremiumFollowPositionRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}		
	};
	
	private AsyncEventProcessor globalEventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PremiumFollowGlobalRequestEvent.class, null);
			subscribeToEvent(PremiumFollowGlobalReplyEvent.class, null);
			subscribeToEvent(PremiumFollowPositionGlobalRequestEvent.class, null);
			subscribeToEvent(PremiumFollowPositionGlobalReplyEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return globalEventManager;
		}		
	};
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) throws Exception {
		Iterator<Entry<String, PremiumFollowGlobalRequestEvent>> it = pendingRequests.entrySet().iterator();
		
		while(it.hasNext()) {
			Entry<String, PremiumFollowGlobalRequestEvent> entry = it.next();
			PremiumFollowGlobalRequestEvent request = entry.getValue();
			if(TimeUtil.getTimePass(request.getTime()) > timeout) {
				pendingRequests.remove(entry.getKey());
				PremiumFollowInfo pf = request.getInfo();
				
				//int error = 201;
				MessageBean errorBean = MessageLookup.lookup(ErrorMessage.PREMIUM_FOLLOW_REQUEST_TIMEOUT);
				String message = errorBean.getMsg()+ " " + pf;
				log.warn("PremiumFollowInfo: " + message);
				
			    message = MessageLookup.buildEventMessage(ErrorMessage.PREMIUM_FOLLOW_REQUEST_TIMEOUT, message);				
				PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(request.getKey(), 
						request.getOriginSender(), null, null, errorBean.getCode(), false, message, request.getUserId(), request.getAccountId(), request.getOriginTxId());
				
				eventManager.sendRemoteEvent(reply);
			}
		}
		
		for(Tx tx : txIdMap.values()){
			if(TimeUtil.getTimePass(tx.date) > timeout)
				txIdMap.remove(tx.txId);
		}
	}

	@Override
	public void init() throws Exception {
		log.info("Initialising...");
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("PremiumFollow-local");

		globalEventProcessor.setHandler(this);
		globalEventProcessor.init();
		if(globalEventProcessor.getThread() != null)
			globalEventProcessor.getThread().setName("PremiumFollow-global");

		scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
	}
		
	public void processPremiumFollowRequestEvent(PremiumFollowRequestEvent event) throws Exception {
		PremiumFollowInfo pf = event.getInfo();
		log.info("Received PremiumFollowRequestEvent: " + pf + ", " + event.getTxId());
		if(null == pf.getMarket()|| null == pf.getFdUser() || event.getTime() == null) {
			//int error = 200;
			MessageBean errorBean = MessageLookup.lookup(ErrorMessage.PREMIUM_FOLLOW_INFO_INCOMPLETE);
			String message = errorBean.getMsg()+ " " + pf;
			log.warn("PremiumFollowInfo: " + message);

		    message = MessageLookup.buildEventMessage(ErrorMessage.PREMIUM_FOLLOW_INFO_INCOMPLETE, message );
	
			PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(event.getKey(), 
					event.getSender(), null, null, errorBean.getCode(), false, message, event.getUserId(), event.getAccountId(), event.getTxId());
			
			eventManager.sendRemoteEvent(reply);
			return;
		}
		
		List<Account> accountList = accountKeeper.getAccounts(pf.getFdUser());
		if(accountList.size() == 0){
			String txId = IdGenerator.getInstance().getNextID();
			PremiumFollowGlobalRequestEvent request = new PremiumFollowGlobalRequestEvent(event.getKey(), null, event.getSender(), pf, event.getUserId(), event.getAccountId(), txId, event.getTxId());
			request.setTime(Clock.getInstance().now());
			pendingRequests.put(txId, request);
			log.info("Fd account is not found in this server, sending global request: " + pf + ", " + txId);
			globalEventManager.sendRemoteEvent(request);
			return;
		}
		
		log.info("Fd account is found in this server: " + pf);

		Account account = accountList.get(0);		
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		
		PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(event.getKey(), 
				event.getSender(), account, positions, 0, true, null, event.getUserId(), event.getAccountId(), event.getTxId());
		eventManager.sendRemoteEvent(reply);
	}
	
	public void processPremiumFollowPositionRequestEvent(PremiumFollowPositionRequestEvent event) throws Exception{
		Map<String, OpenPosition> positionMap = null;
		for(String fdUser : event.getFdUsers()){
			OpenPosition position = getUserPositionBySymbol(fdUser, event.getSymbol());
			if(position != null){
				if(positionMap == null)
					positionMap = new HashMap<String, OpenPosition>();
				positionMap.put(fdUser, position);				
			}
		}
		
		if(positionMap != null){		
			Set<String> findList = positionMap.keySet();
			log.info("Fds position data are found: " + findList.toString());
			if(findList.size() != event.getFdUsers().size()){
				List<String> fdUsers = new ArrayList<String>();
				for(String fdUser : event.getFdUsers()){
					if(!findList.contains(fdUser)){
						fdUsers.add(fdUser);
					}
				}
				String txId = IdGenerator.getInstance().getNextID();
				Tx tx = new Tx();
				tx.txId = txId;
				tx.date = Clock.getInstance().now();
				txIdMap.put(txId, tx);
				log.info("Fds position data are not complete, send global request " + fdUsers.toString() + ", " + txId);
				PremiumFollowPositionGlobalRequestEvent request = new PremiumFollowPositionGlobalRequestEvent(event.getKey(), null, event.getSender(), event.getReqUser(),
						event.getReqAccount(), event.getMarket(), fdUsers, event.getSymbol(), txId, event.getTxId());
				globalEventManager.sendRemoteEvent(request);
			}
			PremiumFollowPositionReplyEvent reply = new PremiumFollowPositionReplyEvent(event.getKey(), event.getSender(),event.getReqUser(), event.getReqAccount(), event.getMarket(), positionMap, event.getSymbol(), event.getTxId());
			eventManager.sendRemoteEvent(reply);
		}else{
			String txId = IdGenerator.getInstance().getNextID();
			Tx tx = new Tx();
			tx.txId = txId;
			tx.date = Clock.getInstance().now();
			txIdMap.put(txId, tx);
			log.info("Fds position data are not found, send global request " + event.getFdUsers().toString() + ", " + txId);
			PremiumFollowPositionGlobalRequestEvent request = new PremiumFollowPositionGlobalRequestEvent(event.getKey(), null, event.getSender(), event.getReqUser(),
					event.getReqAccount(), event.getMarket(), event.getFdUsers(), event.getSymbol(), txId, event.getTxId());
			globalEventManager.sendRemoteEvent(request);
		}
	}
	
	public void processPremiumFollowGlobalRequestEvent(PremiumFollowGlobalRequestEvent event) throws Exception {
		PremiumFollowInfo pf = event.getInfo();
		log.info("Received PremiumFollowGlobalRequestEvent: " + pf);
		
		List<Account> accountList = accountKeeper.getAccounts(pf.getFdUser());
		if(accountList.size() == 0){
			log.info("Global Fd account is not found in this server: " + pf);
			return;
		}

		log.info("Global Fd account is found in this server: " + pf);
		
		Account account = accountList.get(0);		
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		
		log.info("Send Global reply event, key:" + event.getKey() + ", Sender: " + event.getSender());
		PremiumFollowGlobalReplyEvent reply = new PremiumFollowGlobalReplyEvent(event.getKey(), 
				event.getSender(), account, positions, 0, true, null, event.getUserId(), event.getAccountId(), event.getTxId());
		globalEventManager.sendRemoteEvent(reply);
	}

	public void processPremiumFollowGlobalReplyEvent(PremiumFollowGlobalReplyEvent event) throws Exception {
		log.info("Received PremiumFollowGlobalReplyEvent: " + event.getTxId());
		
		PremiumFollowGlobalRequestEvent request = pendingRequests.remove(event.getTxId());
		if(null != request) {
			log.info("processPremiumFollowGlobalReplyEvent found requester: " + request.getTxId());
			
			PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(request.getKey(), 
					request.getOriginSender(), event.getAccount(), event.getPositions(), 0, true, null, request.getUserId(), request.getAccountId(), request.getOriginTxId());
			eventManager.sendRemoteEvent(reply);
		}		
	}
	
	public void processPremiumFollowPositionGlobalRequestEvent(PremiumFollowPositionGlobalRequestEvent event) throws Exception{
		log.info("Received PremiumFollowPositionGlobalRequestEvent: " + event.getTxId());
		Map<String, OpenPosition> positionMap = null;
		for(String fdUser : event.getFdUsers()){
			OpenPosition position = getUserPositionBySymbol(fdUser, event.getSymbol());
			if(position != null){
				if(positionMap == null)
					positionMap = new HashMap<String, OpenPosition>();
				positionMap.put(fdUser, position);				
			}
		}
		
		if(positionMap != null){		
			log.info("Fds position data are found in global request: " + positionMap.keySet().toString());
			PremiumFollowPositionGlobalReplyEvent reply = new PremiumFollowPositionGlobalReplyEvent(event.getKey(), event.getSender(), event.getOriginSender(), 
					event.getReqUser(), event.getReqAccount(), event.getMarket(), positionMap, event.getSymbol(), event.getTxId(), event.getOriginTxId());
			globalEventManager.sendRemoteEvent(reply);
		}else{
			log.info("Fds position data are not found in global request");
		}
	}
	
	public void processPremiumFollowPositionGlobalReplyEvent(PremiumFollowPositionGlobalReplyEvent event) throws Exception{
		log.info("Received PremiumFollowPositionGlobalReplyEvent: " + event.getTxId());
		if(txIdMap.containsKey(event.getTxId())){
			log.info("processPremiumFollowPositionGlobalReplyEvent found requester: " + event.getTxId());
			PremiumFollowPositionReplyEvent reply = new PremiumFollowPositionReplyEvent(event.getKey(), event.getOriginSender(), event.getUser(), event.getAccount(), event.getMarket(), event.getPositionMap(), event.getSymbol(), event.getOriginTxId());
			eventManager.sendRemoteEvent(reply);
		}
	}
	
	
	private OpenPosition getUserPositionBySymbol(String userId, String symbol){
		List<Account> accountList = accountKeeper.getAccounts(userId);
		if(accountList.size() == 0)
			return null;
		Account account = accountList.get(0);
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		for(OpenPosition position : positions){
			if(position.getSymbol().equals(symbol))
				return position;
		}
		return null;
	}
	
	@Override
	public void uninit() {
		scheduleManager.uninit();
		eventProcessor.uninit();
		globalEventProcessor.uninit();	
	}
	
	private class Tx{
		private String txId;
		private Date date;		
	}
	
}
