package com.cyanspring.common.refdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.IRefDataAdaptor;
import com.cyanspring.common.staticdata.IRefDataListener;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger log = LoggerFactory
            .getLogger(RefDataHandler.class);

    @Autowired
    private IRefDataManager refDataManager;

    @Autowired
    private IRemoteEventManager eventManager;

    @Autowired
    private ScheduleManager scheduleManager;

    private MarketSessionType currentType;
    private String tradeDate;
    private IRefDataAdaptor refDataAdaptor;
    private boolean isInit = false;
    private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private long timeInterval = 1*1000;

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
            if (refDataManager.getRefDataList() == null || refDataManager.getRefDataList().size() <= 0)
                ok = false;
            getEventManager().sendLocalOrRemoteEvent(new RefDataEvent(event.getKey(), event.getSender(), refDataManager.getRefDataList(), ok));
            log.info("Response RefDataRequestEvent, ok: {}", ok);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processMarketSessionEvent(MarketSessionEvent event) {
        try {
            if (currentType == null) {
                currentType = event.getSession();
                if(refDataAdaptor == null) {
                    refDataManager.init();
                    refDataManager.update(event.getTradeDate());
                    getEventManager().sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), isInit));
                }
                return;
            }
            if (currentType.equals(event.getSession()) || !MarketSessionType.PREOPEN.equals(event.getSession()))
                return;
            currentType = event.getSession();

            if (refDataManager.update(event.getTradeDate())) {
                if(refDataAdaptor!=null){
                    isInit = false;
                    refDataManager.clearRefData();
                    refDataAdaptor.subscribeRefData(this);
                }
                getEventManager().sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), isInit));
                log.info("Update refData size: {}", refDataManager.getRefDataList().size());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        if(isInit)
            return;
        List<RefData> list = refDataManager.getRefDataList();
        if(list == null)
            return;
        if(list.size() <= 0){
            log.info("RefData size is {}, initial not finish", list.size());
            return;
        } else {
            try {
                getEventManager().sendGlobalEvent(new RefDataEvent(null, null, list, true));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            isInit = true;
        }
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

        if(refDataAdaptor!=null){
            refDataAdaptor.subscribeRefData(this);
        }
        requestRequireData();
    }

    @Override
    public void uninit() {
        if(refDataAdaptor!=null){
            refDataAdaptor.uninit();
        }
        eventProcessor.uninit();
    }

    private void requestRequireData() {
        getEventManager().sendEvent(new MarketSessionRequestEvent(null, null));
    }

    @Override
    public void onRefData(List<RefData> refDataList) throws Exception {
        log.debug("Receive RefData from Adapter - " + refDataList.size());
        if(refDataList == null || refDataList.size()==0){
            refDataAdaptor.uninit();
            refDataAdaptor.subscribeRefData(this);
            return;
        }
        refDataAdaptor.uninit();
        refDataManager.injectRefDataList(refDataList);
    }

    @Override
    public void onRefDataUpdate(List<RefData> refDataList) throws Exception {

    }

    public void setRefDataAdaptor(IRefDataAdaptor refDataAdaptor) {
        this.refDataAdaptor = refDataAdaptor;
    }

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
}
