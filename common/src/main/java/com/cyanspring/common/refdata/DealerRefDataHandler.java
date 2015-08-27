package com.cyanspring.common.refdata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketsession.InternalSessionEvent;
import com.cyanspring.common.event.marketsession.InternalSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent.Action;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataUtil;

/**
 * This Manager is used will receive InternalMarketSessionEvent and get trade date from it,
 * also it will broadcast refDataEvent to it's subscribers. When market session is PREOPEN,
 * it will update refdatas and broadcast out.
 *
 * @author elviswu
 */

public class DealerRefDataHandler implements IPlugin, IRefDataListener {
	private static final Logger log = LoggerFactory.getLogger(DealerRefDataHandler.class);

	@Autowired
	private IRefDataManager refDataManager;

	@Autowired
	private IRemoteEventManager eventManager;

	@Autowired
	private ScheduleManager scheduleManager;

	private List<IRefDataAdaptor> refDataAdaptors;
	private List<RefData> refDataList;
	private Map<String, MarketSessionData> sessionDataMap;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private boolean isInit;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(RefDataRequestEvent.class, null);
			subscribeToEvent(InternalSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public DealerRefDataHandler(List<IRefDataAdaptor> refDataAdaptors) {
		this.refDataAdaptors = refDataAdaptors;
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("DealerRefDataHandler");

		for (IRefDataAdaptor adaptor : refDataAdaptors) {
			adaptor.init();
			adaptor.subscribeRefData(this);
		}
		
		if (!eventProcessor.isSync()) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, 1);
			scheduleManager.scheduleTimerEvent(cal.getTime(), eventProcessor, timerEvent);
		}
	}

	@Override
	public void uninit() {
		for (IRefDataAdaptor adaptor : refDataAdaptors)
			adaptor.uninit();
		eventProcessor.uninit();
	}

	@Override
	public void onRefData(List<RefData> refDataList) throws Exception {
		if (refDataList == null || refDataList.size() == 0)
			return;
		if (this.refDataList == null)
			this.refDataList = new ArrayList<>();
		log.debug("Receive RefData from Adapter - " + refDataList.size());
		for (RefData refData : refDataList) {
			if (checkRefData(refData))
				this.refDataList.add(refData);
		}
	}

	@Override
	public void onRefDataUpdate(List<RefData> refDataList, Action action) {
		if (refDataList != null && refDataList.size() > 0) {
			log.debug("Receive RefDataUpdate from Adapter - " + refDataList.size() + ", action: " + action.toString());
			try {
				List<RefData> send = new ArrayList<>();
				for (RefData refData : refDataList) {
					if (!checkRefData(refData))
						continue;
					String index = RefDataUtil.getCategory(refData);
					if (index == null)
						throw new Exception("RefData index not find");
					String tradeDate = sessionDataMap.get(index).getTradeDateByString();

					if (action == Action.ADD || action == Action.MOD) {
						refDataManager.update(refData, tradeDate);
						send.add(refData);
					} else if (action == Action.DEL) {
						refDataManager.remove(refData);
						send.add(refData);
					} else {
						log.error("Unknow action for RefData update");
					}
				}
				RefDataUpdateEvent event = new RefDataUpdateEvent(null, null, send, action);
				eventManager.sendGlobalEvent(event);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private boolean checkRefData(RefData refData) {
		if (!StringUtils.hasText(refData.getRefSymbol()) || !StringUtils.hasText(refData.getCNDisplayName())
				|| !StringUtils.hasText(refData.getExchange()) || !StringUtils.hasText(refData.getCode())
				|| !StringUtils.hasText(refData.getIType())) {

			log.error("Incorrect refData from adaptor.");
			return false;
		}
		return true;
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (sessionDataMap == null) {
			eventManager.sendEvent(new InternalSessionRequestEvent(null, null));
			scheduleNextCheck();
			return;
		}

		for (IRefDataAdaptor adaptor : refDataAdaptors) {
			if (!adaptor.getStatus()) {
				scheduleNextCheck();
				return;
			}
		}

		refDataManager.injectRefDataList(refDataList);
		try {
			refDataManager.init();
			for (RefData refData : refDataList) {
				String index = refData.getCategory();
				if (index == null)
					throw new Exception("RefData index not find");
				MarketSessionData session = sessionDataMap.get(index);
				if (session == null) {
					log.warn("Can't find market session data for [" + index + "], remove it from list");
					refDataManager.remove(refData);
					continue;
				}
				String tradeDate = session.getTradeDateByString();
				refDataManager.update(refData, tradeDate);
			}
			eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			scheduleNextCheck();
			return;
		}
		isInit = true;
	}

	private void scheduleNextCheck() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 1);
		scheduleManager.scheduleTimerEvent(cal.getTime(), eventProcessor, timerEvent);
	}

	public void processRefDataRequestEvent(RefDataRequestEvent event) {
		try {
			boolean ok = true;
			if (refDataManager.getRefDataList() == null || refDataManager.getRefDataList().size() <= 0)
				ok = false;
			eventManager.sendLocalOrRemoteEvent(
					new RefDataEvent(event.getKey(), event.getSender(), refDataManager.getRefDataList(), ok));
			log.info("Response RefDataRequestEvent, ok: {}", ok);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processInternalSessionEvent(InternalSessionEvent event) {
		Map<String, MarketSessionData> sessions = event.getDataMap();
		if (sessionDataMap == null) {
			sessionDataMap = sessions;
			return;
		}
		if (!isInit)
			return;

		List<RefData> send = new ArrayList<>();
		for (Entry<String, MarketSessionData> session : sessions.entrySet()) {
			String index = session.getKey();
			MarketSessionType type = session.getValue().getSessionType();
			sessionDataMap.put(index, session.getValue());
			if (type != MarketSessionType.PREOPEN)
				continue;
			log.info(index + ", " + type.toString());
			try {
				List<RefData> update = refDataManager.update(index, session.getValue().getTradeDateByString());
				send.addAll(update);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if (send.size() > 0) {
			try {
				eventManager.sendGlobalEvent(new RefDataEvent(null, null, send, true));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
