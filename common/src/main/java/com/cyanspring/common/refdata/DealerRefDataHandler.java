package com.cyanspring.common.refdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketsession.InternalSessionEvent;
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

public class DealerRefDataHandler implements IPlugin, IRefDataListener {
	private static final Logger log = LoggerFactory.getLogger(DealerRefDataHandler.class);

	@Autowired
	private IRefDataManager refDataManager;

	@Autowired
	private IRemoteEventManager eventManager;

	private List<IRefDataAdaptor> refDataAdaptors;
	private List<RefData> refDataList;
	private Map<String, MarketSessionData> sessionDataMap;
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

		for (IRefDataAdaptor adaptor : refDataAdaptors)
			adaptor.subscribeRefData(this);
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

		this.refDataList.addAll(refDataList);
		isInit();
	}
	
	@Override
	public void onRefDataUpdate(List<RefData> refDataList, Action action) {
		if (refDataList != null && refDataList.size() > 0) {
			log.debug("Receive RefDataUpdate from Adapter - " + refDataList.size());
			RefDataUpdateEvent event = new RefDataUpdateEvent(null, null, refDataList, RefDataUpdateEvent.Action.ADD);
//			eventManager.sendGlobalEvent(event);
		}
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
	
	private boolean isInit(){
		if (!isInit) {
			for (IRefDataAdaptor adaptor : refDataAdaptors){
				if(!adaptor.getStatus()){
					return false;
				}
			}
			try {
				refDataManager.init();
				for (RefData refData : refDataList) {
					String index = refData.getCategory();
					if (index == null)
						index = RefDataUtil.getOnlyChars(refData.getENDisplayName());
					if (index == null)
						throw new Exception("RefData index not find");
					String tradeDate = sessionDataMap.get(index).getTradeDateByString();
					refDataManager.update(refData, tradeDate);
				}
				eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			isInit = true;
		}
		return isInit;
	}

	public void processInternalSessionEvent(InternalSessionEvent event) {
		Map<String, MarketSessionData> sessions = event.getDataMap();
		if (sessionDataMap == null) {
			sessionDataMap = sessions;
			return;
		}
		if (!isInit())
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
