package com.cyanspring.common.refdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.InternalSessionEvent;
import com.cyanspring.common.event.marketsession.InternalSessionRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent.Action;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This Manager is used to send detail refData to the subscriber
 *
 * BoardCastEvent: 1) RefDataEvent Event can be request: 1) RefDataRequestEvent
 * Subscribed Event: 1) MarketSessionEvent
 *
 * @author elviswu
 * @version 1.1, modify by shuwei.kuo
 * @since 1.0
 */

public class FileRefDataHandler implements IPlugin {
	private static final Logger log = LoggerFactory.getLogger(FileRefDataHandler.class);

	@Autowired
	private IRefDataManager refDataManager;

	@Autowired
	private IRemoteEventManager eventManager;

	private MarketSessionType currentType;
	private Map<String, MarketSessionData> rawMap;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(RefDataRequestEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
			subscribeToEvent(InternalSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

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

	public void processMarketSessionEvent(MarketSessionEvent event) {
		try {
			if (currentType == null) {
				currentType = event.getSession();
				refDataManager.init();
				refDataManager.updateAll(event.getTradeDate());
				eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
				return;
			}
			if (currentType.equals(event.getSession()) || !MarketSessionType.PREOPEN.equals(event.getSession()))
				return;
			currentType = event.getSession();

			if (refDataManager.updateAll(event.getTradeDate())) {
				eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
				log.info("Update refData size: {}", refDataManager.getRefDataList().size());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processInternalSessionEvent(InternalSessionEvent event) {
		try {
			if (rawMap == null) {
				rawMap = event.getDataMap();
				refDataManager.init();
				for (Entry<String, MarketSessionData> e : event.getDataMap().entrySet()) {
					MarketSessionData data = e.getValue();
					refDataManager.update(e.getKey(), data.getTradeDateByString());
				}
				eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
			} else {
				List<RefData> send = new ArrayList<>();
				for (Entry<String, MarketSessionData> e : event.getDataMap().entrySet()) {
					MarketSessionData data = e.getValue();
					rawMap.put(e.getKey(), data);
					if (!data.getSessionType().equals(MarketSessionType.PREOPEN))
						continue;
					log.info("Update refData index: {}", e.getKey());
					refDataManager.update(e.getKey(), data.getTradeDateByString());
					for (RefData refData : refDataManager.getRefDataList()) {
						if (e.getKey().equals(refData.getCategory()))
							send.add(refData);
					}

				}

				if (send.size() > 0)
					eventManager.sendGlobalEvent(new RefDataUpdateEvent(null, null, send, Action.MOD));
			}
		} catch (Exception err) {
			log.error(err.getMessage(), err);
		}
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("FileRefDataHandler");

		requestRequireData();
	}

	@Override
	public void uninit() {
		eventProcessor.uninit();
	}

	private void requestRequireData() {
		eventManager.sendEvent(new MarketSessionRequestEvent(null, null));
		eventManager.sendEvent(new InternalSessionRequestEvent(null, null));
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
}
