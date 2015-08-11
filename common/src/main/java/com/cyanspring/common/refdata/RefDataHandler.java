package com.cyanspring.common.refdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent;
import com.cyanspring.common.event.refdata.RefDataUpdateEvent.Action;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * This Manager is used to send detail refData to the subscriber
 *
 * BoardCastEvent: 1) RefDataEvent 
 * Event can be request: 1) RefDataRequestEvent
 * Subscribed Event: 1) MarketSessionEvent
 *
 * @author elviswu
 * @version 1.1, modify by shuwei.kuo
 * @since 1.0
 */

public class RefDataHandler implements IPlugin, IRefDataListener {
	private static final Logger log = LoggerFactory.getLogger(RefDataHandler.class);

	@Autowired
	private IRefDataManager refDataManager;

	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	private ScheduleManager scheduleManager;

	private MarketSessionType currentType;
	private List<IRefDataAdaptor> refDataAdaptors;
	private List<RefData> refDataList;
	private boolean isUpdated = false;
	private boolean isSent = false;
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long timeInterval = 1 * 1000;
	private String tradeDate;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(RefDataRequestEvent.class, null);
			subscribeToEvent(MarketSessionEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	public void processRefDataRequestEvent(RefDataRequestEvent event) {
		try {
			boolean ok = true;
			if (refDataManager.getRefDataList() == null || refDataManager.getRefDataList().size() <= 0 || !isUpdated)
				ok = false;
			eventManager.sendLocalOrRemoteEvent(
					new RefDataEvent(event.getKey(), event.getSender(), refDataManager.getRefDataList(), ok));
			log.info("Response RefDataRequestEvent, ok: {}", ok);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void processMarketSessionEvent(MarketSessionEvent event) {
		currentType = event.getSession();
		tradeDate = event.getTradeDate();
		if (currentType.equals(event.getSession()) || !MarketSessionType.PREOPEN.equals(event.getSession()))
			return;

		for (IRefDataAdaptor adaptor : refDataAdaptors) {
			adaptor.flush();
			refDataList = null;
			refDataManager.clearRefData();
			isUpdated = false;
			try {
				adaptor.subscribeRefData(this);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		if (!isUpdated || isSent)
			return;
		try {
			refDataManager.init();
			refDataManager.updateAll(tradeDate);
			eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), isUpdated));
			isSent = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
//		isUpdated = false;
	}

	@Override
	public void init() throws Exception {
		log.info("initialising");

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("RefDataHandler");
		
        if(!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timeInterval, eventProcessor, timerEvent);

		for (IRefDataAdaptor adaptor : refDataAdaptors)
			adaptor.subscribeRefData(this);

		requestRequireData();
	}

	@Override
	public void uninit() {
		for (IRefDataAdaptor adaptor : refDataAdaptors)
			adaptor.uninit();
		eventProcessor.uninit();
	}

	private void requestRequireData() {
		eventManager.sendEvent(new MarketSessionRequestEvent(null, null));
	}

	@Override
	public void onRefData(List<RefData> refDataList) throws Exception {
		log.debug("Receive RefData from Adapter - " + refDataList.size());

		if (this.refDataList == null)
			this.refDataList = refDataList;
		else
			this.refDataList.addAll(0, refDataList);

		for (IRefDataAdaptor adaptor : refDataAdaptors) {
			if (!adaptor.getStatus())
				return;
		}

		isUpdated = true;
		isSent = false;
		refDataManager.injectRefDataList(this.refDataList);
	}

	@Override
	public void onRefDataUpdate(List<RefData> refDataList) throws Exception {
		List<RefData> send = new ArrayList<>();
		for (RefData refData : refDataList) {
			if(!this.refDataList.contains(refData)){
				refDataManager.update(refData, tradeDate);
				send.add(refData);
			}
		}
		
		eventManager.sendGlobalEvent(new RefDataUpdateEvent(null, null, send, Action.ADD));
	}

	public void setRefDataAdaptors(List<IRefDataAdaptor> refDataAdaptors) {
		this.refDataAdaptors = refDataAdaptors;
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
}
