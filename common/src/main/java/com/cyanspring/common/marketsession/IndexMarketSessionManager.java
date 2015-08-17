package com.cyanspring.common.marketsession;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

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
	private Map<String, MarketSessionData> sessionDataMap;
	private Map<String, Date> checkDateMap = new HashMap<>();
	private Map<String, RefData> refDataMap;
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
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processIndexSessionRequestEvent(IndexSessionRequestEvent event) {
		try {
			if (checkSessionAndRefData()) {
				eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(), null, false));
				return;
			}

			if (event.getIndexList() == null) {
				eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(), sessionDataMap, false));
				return;
			}
			
			List<RefData> refDataList = new ArrayList<>();
			for (String index : event.getIndexList()) 
				refDataList.add(refDataMap.get(index));
		
			Map<String, MarketSessionData> send = new HashMap<>();			
			for (RefData refData : refDataList){
				MarketSessionData data = sessionDataMap.get(refData.getSymbol());
				if (data == null) {
					log.error("Request data not complete, index: {}", refData.getSymbol());
				}
				send.put(refData.getSymbol(), data);
			}
			
			eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(), send, false));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processAllIndexSessionRequestEvent(AllIndexSessionRequestEvent event) {
		try {
			eventManager.sendLocalOrRemoteEvent(new AllIndexSessionEvent(event.getKey(), event.getSender(), sessionDataMap));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processRefDataEvent(RefDataEvent event) {
		if (!event.isOk())
			return;
		refDataMap = new HashMap<String, RefData>();
		for (RefData refData : event.getRefDataList()) {
			refDataMap.put(refData.getRefSymbol(), refData);
		}
	}

	public void processPmSettlementEvent(PmSettlementEvent event) {
		log.info("Receive PmSettlementEvent, symbol: " + event.getEvent().getSymbol());
		eventManager.sendEvent(event.getEvent());
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		try {
			checkIndexMarketSession();
			checkSettlement();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void checkIndexMarketSession() {
		if (checkSessionAndRefData())
			return;
		try {
			if (refDataMap == null || refDataMap.size() <= 0)
				return;
			if (sessionDataMap == null) {
					sessionDataMap = marketSessionUtil
							.getMarketSession(new ArrayList<RefData>(refDataMap.values()), Clock.getInstance().now());
				eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, sessionDataMap, true));
				return;
			}

			Map<String, MarketSessionData> sendMap = new HashMap<String, MarketSessionData>();
			Map<String, MarketSessionData> chkMap;
			chkMap = marketSessionUtil.getMarketSession(new ArrayList<RefData>(refDataMap.values()), Clock.getInstance().now());

			for (Map.Entry<String, MarketSessionData> entry : chkMap.entrySet()) {
				MarketSessionData chkData = sessionDataMap.get(entry.getKey());
				if (chkData == null || !chkData.getSessionType().equals(entry.getValue().getSessionType())) {
					sessionDataMap.put(entry.getKey(), entry.getValue());
					sendMap.put(entry.getKey(), entry.getValue());
				}
			}

			if (sendMap.size() > 0) {
				log.info("Update indexMarketSession size:{}, keys: {}", sendMap.size(), sendMap.keySet());
				eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, sendMap, true));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void checkSettlement() throws ParseException {
		if (noCheckSettlement)
			return;
		if (checkRefData())
			return;
		
		List<RefData> refDataList = new ArrayList<RefData>(refDataMap.values());
		for (RefData refData : refDataList) {
			String index = refData.getSymbol();
			if (refData.getSettlementDate() == null)
				continue;
			MarketSessionData data = sessionDataMap.get(index);
			Date chkDate = checkDateMap.get(index);
			if (chkDate == null) {
				chkDate = TimeUtil.getPreviousDay();
			}
			
			if (TimeUtil.sameDate(chkDate, data.getTradeDateByDate()) || data.getSessionType().equals(MarketSessionType.CLOSE)) 
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

	private boolean checkSessionAndRefData() {
		return marketSessionUtil == null || checkRefData();
	}

	private boolean checkRefData() {
		return refDataMap == null || refDataMap.size() <= 0;
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
}
