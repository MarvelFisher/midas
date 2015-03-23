package com.cyanspring.server.account;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PremiumFollowInfo;
import com.cyanspring.common.error.ErrorLookup;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.account.PremiumFollowGlobalReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowGlobalRequestEvent;
import com.cyanspring.common.event.account.PremiumFollowReplyEvent;
import com.cyanspring.common.event.account.PremiumFollowRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;

public class PremiumFollowManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(PremiumFollowManager.class);

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private IRemoteEventManager globalEventManager;
	
	@Autowired
	private PositionKeeper positionKeeper;
	
	@Autowired
	private AccountKeeper accountKeeper;
	
	private Map<String, PremiumFollowRequestEvent> pendingRequests = 
				new HashMap<String, PremiumFollowRequestEvent>();
	private ScheduleManager scheduleManager = new ScheduleManager();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long timerInterval = 5000;
	private long timeout = 10000;
//	private Map<String, Map<String, PremiumFollowInfo>> followers = 
//			new HashMap<String, Map<String, PremiumFollowInfo>>(); // fdAccount/frAccount
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(PremiumFollowRequestEvent.class, null);
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
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return globalEventManager;
		}
		
	};
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) throws Exception {
		Iterator<Entry<String, PremiumFollowRequestEvent>> it = pendingRequests.entrySet().iterator();
		
		while(it.hasNext()) {
			Entry<String, PremiumFollowRequestEvent> entry = it.next();
			PremiumFollowRequestEvent request = entry.getValue();
			if(TimeUtil.getTimePass(request.getTime()) > timeout) {
				pendingRequests.remove(entry.getKey());
				PremiumFollowInfo pf = request.getInfo();
				int error = 201;
				String message = ErrorLookup.lookup(error) + " " + pf;
				log.warn("PremiumFollowInfo: " + message);
				PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(request.getKey(), 
						request.getSender(), null, null, error, false, message, request.getUserId(), request.getAccountId(), request.getTxId());
				
				eventManager.sendRemoteEvent(reply);
			}
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
	
//	private void updateFollower(PremiumFollowInfo pf) {
//		Map<String, PremiumFollowInfo> map = followers.get(pf.getFdAccount());
//		if(null == map) {
//			map = new HashMap<String, PremiumFollowInfo>();
//			followers.put(pf.getFdAccount(), map);
//		}
//		
//		PremiumFollowInfo existing = map.get(pf.getFrAccount());
//		
//		if(null == existing || pf.getExpiry() != null)
//			map.put(pf.getFrAccount(), pf);
//	}
	
	public void processPremiumFollowRequestEvent(PremiumFollowRequestEvent event) throws Exception {
		PremiumFollowInfo pf = event.getInfo();
		log.info("Received PremiumFollowRequestEvent: " + pf + ", " + event.getTxId());
		if(null == pf.getMarket()|| null == pf.getFdUser() || event.getTime() == null) {
			int error = 200;
			String message = ErrorLookup.lookup(error) + " " + pf;
			log.warn("PremiumFollowInfo: " + message);
			PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(event.getKey(), 
					event.getSender(), null, null, error, false, message, event.getUserId(), event.getAccountId(), event.getTxId());
			
			eventManager.sendRemoteEvent(reply);
		}
		
		//!!! TODO: also check expiry
		if(!accountKeeper.accountExists(pf.getFdUser() + "-" + pf.getMarket())
				&& accountKeeper.getAccounts(pf.getFdUser()).size() == 0) { // fd account doesn't exist in this server
			String txId = IdGenerator.getInstance().getNextID();
			PremiumFollowGlobalRequestEvent request = new PremiumFollowGlobalRequestEvent(event.getKey(), null, pf, event.getUserId(), event.getAccountId(), txId);
			request.setTime(Clock.getInstance().now());
			pendingRequests.put(txId, request);
			log.info("Fd account is not found in this server, sending global request: " + pf + ", " + txId);
			globalEventManager.sendRemoteEvent(request);
			return;
		}
		
		// fd account is in this server
		log.info("Fd account is found in this server: " + pf);

//		 !!! TODO: send an event to persistence manager to persist premium info
		
//		updateFollower(pf);
		Account account = accountKeeper.getAccount(pf.getFdUser() + "-" + pf.getMarket());
		if(account == null)
			account = accountKeeper.getAccounts(pf.getFdUser()).get(0);
		
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		
		PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(event.getKey(), 
				event.getSender(), account, positions, 0, true, null, event.getUserId(), event.getAccountId(), event.getTxId());
		eventManager.sendRemoteEvent(reply);
	}
	
	public void processPremiumFollowGlobalRequestEvent(PremiumFollowGlobalRequestEvent event) throws Exception {
		PremiumFollowInfo pf = event.getInfo();
		log.info("Received PremiumFollowGlobalRequestEvent: " + pf);
		
		//!!! TODO: also check expiry
		if(!accountKeeper.accountExists(pf.getFdUser() + "-" + pf.getMarket())
				&& accountKeeper.getAccounts(pf.getFdUser()).size() == 0) { // fd account doesn't exist in this server
			log.info("Global Fd account is not found in this server: " + pf);
			return;
		}

		// !!! TODO: send an event to persistence manager to persist premium info

		// fd account is in this server
		log.info("Global Fd account is found in this server: " + pf);

//		updateFollower(pf);

		Account account = accountKeeper.getAccount(pf.getFdUser() + "-" + pf.getMarket());
		if(account == null)
			account = accountKeeper.getAccounts(pf.getFdUser()).get(0);
		
		List<OpenPosition> positions = positionKeeper.getOverallPosition(account);
		
		PremiumFollowGlobalReplyEvent reply = new PremiumFollowGlobalReplyEvent(event.getKey(), 
				event.getSender(), account, positions, 0, true, null, event.getUserId(), event.getAccountId(), event.getTxId());
		eventManager.sendRemoteEvent(reply);
	}

	public void processPremiumFollowGlobalReplyEvent(PremiumFollowGlobalReplyEvent event) throws Exception {
		log.info("Received PremiumFollowGlobalRequestEvent: " + event.getTxId());
		
		PremiumFollowRequestEvent request = pendingRequests.remove(event.getTxId());
		if(null != request) {
			log.info("processPremiumFollowGlobalReplyEvent found requester: " + request.getTxId());
			
			PremiumFollowReplyEvent reply = new PremiumFollowReplyEvent(request.getKey(), 
					request.getSender(), event.getAccount(), event.getPositions(), 0, true, null, request.getUserId(), request.getAccountId(), request.getTxId());
			eventManager.sendRemoteEvent(reply);
		}
		
	}
	
	@Override
	public void uninit() {
		eventProcessor.uninit();
		globalEventProcessor.uninit();
		
		
	}

}
