package com.cyanspring.server.refdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.event.AsyncEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This Manager is used to send detail refData to the subscriber
 *
 * BoardCastEvent: 1) RefDataEvent
 * Event can be request: 1) RefDataRequestEvent
 * Subscribed Event: 1) MarketSessionEvent
 *
 * @author elviswu
 * @version 1.0, modify by elviswu
 * @since 1.0
 */

public class RefDataHandler implements IPlugin {
    private static final Logger log = LoggerFactory
            .getLogger(RefDataHandler.class);

    @Autowired
    private IRefDataManager refDataManager;

    @Autowired
    private IRemoteEventManager eventManager;

    private MarketSessionType currentType;
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
            if (refDataManager.getRefDataList() == null || refDataManager.getRefDataList().size() <= 0)
                ok = false;
            eventManager.sendLocalOrRemoteEvent(new RefDataEvent(event.getKey(), event.getSender(), refDataManager.getRefDataList(), ok));
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
                refDataManager.update(event.getTradeDate());
                eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
                return;
            }
            if (currentType.equals(event.getSession()) || !MarketSessionType.PREOPEN.equals(event.getSession()))
                return;
            currentType = event.getSession();

            if (refDataManager.update(event.getTradeDate())) {
                eventManager.sendGlobalEvent(new RefDataEvent(null, null, refDataManager.getRefDataList(), true));
                log.info("Update refData size: {}", refDataManager.getRefDataList().size());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

        requestRequireData();
    }

    @Override
    public void uninit() {
        eventProcessor.uninit();
    }

    private void requestRequireData() {
        eventManager.sendEvent(new MarketSessionRequestEvent(null, null));
    }
}
