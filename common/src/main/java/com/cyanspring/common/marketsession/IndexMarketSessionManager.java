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
import com.cyanspring.common.staticdata.IRefDataManager;
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
			subscribeToEvent(RefDataUpdateEvent.class, null);
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
				eventManager.sendLocalOrRemoteEvent(
						new IndexSessionEvent(event.getKey(), event.getSender(), sessionDataMap, false));
				return;
			}

			List<RefData> refDataList = new ArrayList<>();
			for (String index : event.getIndexList())
				refDataList.add(refDataMap.get(index));

			Map<String, MarketSessionData> send = new HashMap<>();
			for (RefData refData : refDataList) {
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
			eventManager.sendLocalOrRemoteEvent(
					new AllIndexSessionEvent(event.getKey(), event.getSender(), sessionDataMap));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processRefDataEvent(RefDataEvent event) {
		if (!event.isOk())
			return;
		if (refDataMap == null)
			refDataMap = new HashMap<String, RefData>();

		List<RefData> refDataList = event.getRefDataList();
		sendIndexMarketSession(refDataList);
	}

	public void processRefDataUpdateEvent(RefDataUpdateEvent event) {
		if (event.getAction() == Action.ADD) {
			List<RefData> list = event.getRefDataList();
			sendIndexMarketSession(list);
		}
	}
	
	private void sendIndexMarketSession(List<RefData> refDataList) {
		Map<String, MarketSessionData> send = new HashMap<>();
		try {
			for (RefData refData : refDataList) {
				refDataMap.put(refData.getSymbol(), refData);
				if (refData.getIndexSessionType().equals(IndexSessionType.SETTLEMENT.toString())) {
					MarketSessionData session = marketSessionUtil.getMarketSession(refData, Clock.getInstance().now());
					send.put(refData.getSymbol(), session);
				} else {
					MarketSessionData session = send.get(refData.getCategory());
					if (session == null) {
						session = marketSessionUtil.getMarketSession(refData, Clock.getInstance().now());
						send.put(refData.getCategory(), session);
					}
				}
			}

			if (send.size() > 0) 
				eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, send, true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
		if (marketSessionUtil == null)
			return;
		try {
			if (sessionDataMap == null) {
				sessionDataMap = marketSessionUtil.getMarketSession();
				eventManager.sendEvent(new InternalSessionEvent(null, null, sessionDataMap, true));
				return;
			}

			Map<String, MarketSessionData> cache = marketSessionUtil.getMarketSession();
			Map<String, MarketSessionData> send = new HashMap<>();

			for (Entry<String, MarketSessionData> session : sessionDataMap.entrySet()) {
				MarketSessionData c = cache.get(session.getKey());
				if (!c.getSessionType().equals(session.getValue().getSessionType())) {
					sessionDataMap.put(session.getKey(), c);
					send.put(session.getKey(), c);
				}
			}

			if (send.size() > 0)
				eventManager.sendEvent(new InternalSessionEvent(null, null, send, true));

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
