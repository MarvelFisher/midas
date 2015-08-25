package com.cyanspring.common.marketsession;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent.Action;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.fu.IndexSessionType;
import com.cyanspring.common.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This Manager is used to send detail marketsession to the subscriber and this
 * manager will check settlement day to monitor every refdatas.
 *
 * BoardCastEvent: 1) IndexSessionEvent Event can be request: 1)
 * IndexSessionRequestEvent Subscribed Event: 1) MarketSessionEvent, 2)
 * RefDataEvent
 *
 * @author elviswu
 * @version 1.0, modify by elviswu
 * @since 1.0
 */
public class IndexMarketSessionManager implements IPlugin {
	private static final Logger log = LoggerFactory.getLogger(IndexMarketSessionManager.class);

	private IRemoteEventManager eventManager;

	@Autowired
	private MarketSessionUtil marketSessionUtil;

	private boolean noCheckSettlement = false;
	private ScheduleManager scheduleManager = new ScheduleManager();
	private Map<String, MarketSessionData> rawMap;
	private Map<String, MarketSessionData> currentSessionMap = new HashMap<>();
	private Queue<RefData> addQueue = new LinkedBlockingQueue<>();
	private Queue<RefData> delQueue = new LinkedBlockingQueue<>();
	private Map<String, Date> checkDateMap = new HashMap<>();
	private Map<String, RefData> refDataMap = new HashMap<String, RefData>();;
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	protected long timerInterval = 1 * 1000;
	private int settlementDelay = 10;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(IndexSessionRequestEvent.class, null);
			subscribeToEvent(AllIndexSessionRequestEvent.class, null);
			subscribeToEvent(RefDataEvent.class, null);
			subscribeToEvent(RefDataUpdateEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processIndexSessionRequestEvent(IndexSessionRequestEvent event) {
		try {
			if (!checkUtilAndRefData() && !checkCurrentSession()) {
				Map<String, MarketSessionData> send = new HashMap<>();
				eventManager
						.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(), send, false));
				return;
			}

			if (event.getIndexList() == null) {
				eventManager.sendLocalOrRemoteEvent(
						new IndexSessionEvent(event.getKey(), event.getSender(), currentSessionMap, true));
				return;
			}

			List<RefData> refDataList = new ArrayList<>();
			for (String index : event.getIndexList())
				refDataList.add(refDataMap.get(index));

			Map<String, MarketSessionData> send = new HashMap<>();
			for (RefData refData : refDataList) {
				MarketSessionData data = currentSessionMap.get(refData.getSymbol());
				if (data == null) {
					log.error("Request data not complete, index: {}", refData.getSymbol());
				}
				send.put(refData.getSymbol(), data);
			}

			eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(), send, true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processAllIndexSessionRequestEvent(AllIndexSessionRequestEvent event) {
		try {
			eventManager.sendLocalOrRemoteEvent(
					new AllIndexSessionEvent(event.getKey(), event.getSender(), currentSessionMap));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processRefDataEvent(RefDataEvent event) {
		if (!event.isOk())
			return;

		for (RefData refData : event.getRefDataList()) 
			addQueue.offer(refData);
	}

	public void processRefDataUpdateEvent(RefDataUpdateEvent event) {
		List<RefData> list = event.getRefDataList();
		if (event.getAction() == Action.ADD) {
			for (RefData refData : list) 
				addQueue.offer(refData);
		} else if (event.getAction() == Action.MOD) {
			for (RefData refData : list) 
				addQueue.offer(refData);
		} else if (event.getAction() == Action.DEL) {
			for (RefData refData : list) 
				delQueue.offer(refData);
		}
	}

	private String getIndex(RefData refData) {
		String index = null;
		if (refData.getIndexSessionType().equals(IndexSessionType.SETTLEMENT.toString()))
			index = refData.getSymbol();
		else if (refData.getIndexSessionType().equals(IndexSessionType.SPOT.toString()))
			index = refData.getCategory();
		else if (refData.getIndexSessionType().equals(IndexSessionType.EXCHANGE.toString()))
			index = refData.getExchange();
		return index;
	}

	public void processPmSettlementEvent(PmSettlementEvent event) {
		log.info("Receive PmSettlementEvent, symbol: " + event.getEvent().getSymbol());
		eventManager.sendEvent(event.getEvent());
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		try {
			checkInteralIndexMarketSession();
			delRefData();
			addRefData();
			checkIndexMarketSession();
			checkSettlement();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void checkInteralIndexMarketSession() throws Exception {
		if (marketSessionUtil == null)
			return;
		if (rawMap == null) {
			rawMap = marketSessionUtil.getMarketSession();
			eventManager.sendEvent(new InternalSessionEvent(null, null, rawMap, true));
			return;
		}

		Map<String, MarketSessionData> cache = marketSessionUtil.getMarketSession();
		Map<String, MarketSessionData> send = new HashMap<>();

		for (Entry<String, MarketSessionData> session : rawMap.entrySet()) {
			MarketSessionData c = cache.get(session.getKey());
			if (!c.getSessionType().equals(session.getValue().getSessionType())) {
				rawMap.put(session.getKey(), c);
				send.put(session.getKey(), c);
			}
		}

		if (send.size() > 0)
			eventManager.sendEvent(new InternalSessionEvent(null, null, send, true));
	}

	private void checkIndexMarketSession() throws Exception {
		if (!checkUtilAndRefData())
			return;

		Map<String, MarketSessionData> send = new HashMap<>();
		for (RefData refData : refDataMap.values()) {
			MarketSessionData session = marketSessionUtil.getMarketSession(refData, Clock.getInstance().now());
			String index = getIndex(refData);
			MarketSessionData current = currentSessionMap.get(index);			
			if (current == null) {
				send.put(index, session);
				currentSessionMap.put(index, session);
				continue;
			}
			if (!current.getSessionType().equals(session.getSessionType())) {
				send.put(index, session);
				currentSessionMap.put(index, session);
			}
		}

		if (send.size() > 0)
			eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, send, true));
	}
	
	private void addRefData(){
		RefData refData;
		while((refData = addQueue.poll()) != null) {
			refDataMap.put(refData.getSymbol(), refData);
		}
	}
	
	private void delRefData(){
		RefData refData;
		while((refData = delQueue.poll()) != null) {
			refDataMap.remove(refData.getSymbol(), refData);
		}
	}

	private void checkSettlement() throws ParseException {
		if (noCheckSettlement)
			return;
		if (!checkRefData())
			return;

		List<RefData> refDataList = new ArrayList<RefData>(refDataMap.values());
		for (RefData refData : refDataList) {
			String index = refData.getSymbol();
			if (refData.getSettlementDate() == null)
				continue;
			MarketSessionData data = rawMap.get(index);
			if (data == null)
				data = rawMap.get(refData.getCategory());
			if (data == null)
				data = rawMap.get(refData.getExchange());
			if (data == null) {
				log.error(index + ", check settlement day fail");
				continue;
			}
			Date chkDate = checkDateMap.get(index);
			if (chkDate == null) {
				chkDate = TimeUtil.getPreviousDay();
			}

			if (TimeUtil.sameDate(chkDate, data.getTradeDateByDate())
					|| data.getSessionType().equals(MarketSessionType.CLOSE))
				continue;
			chkDate = data.getTradeDateByDate();
			checkDateMap.put(index, chkDate);

			Date settlementDate = sdf.parse(refData.getSettlementDate());
			if (TimeUtil.sameDate(settlementDate, chkDate)) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, settlementDelay);
				SettlementEvent sdEvent = new SettlementEvent(null, null, refData.getSymbol());
				PmSettlementEvent pmSDEvent = new PmSettlementEvent(null, null, sdEvent);
				scheduleManager.scheduleTimerEvent(cal.getTime(), eventProcessor, pmSDEvent);
				log.info("Start SettlementEvent after " + settlementDelay + " mins, symbol: " + refData.getSymbol());
				currentSessionMap.remove(refData.getSymbol());
			}
		}
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("IndexMarketSessionManager");

		if (!eventProcessor.isSync())
			scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
	}

	@Override
	public void uninit() {
		if (!eventProcessor.isSync())
			scheduleManager.uninit();
		eventProcessor.uninit();
	}

	public void setNoCheckSettlement(boolean noCheckSettlement) {
		this.noCheckSettlement = noCheckSettlement;
	}

	public void setSettlementDelay(int settlementDelay) {
		this.settlementDelay = settlementDelay;
	}

	private boolean checkUtilAndRefData() {
		return marketSessionUtil != null && checkRefData();
	}

	private boolean checkRefData() {
		return refDataMap != null && refDataMap.size() > 0;
	}
	
	private boolean checkCurrentSession() {
		return currentSessionMap != null && currentSessionMap.size() > 0; 
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
}
