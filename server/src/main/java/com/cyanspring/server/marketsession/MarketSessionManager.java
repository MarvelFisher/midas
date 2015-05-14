/**
 * ****************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * <p/>
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * ****************************************************************************
 */
package com.cyanspring.server.marketsession;

import java.util.*;

import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.marketsession.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.AsyncEventProcessor;

public class MarketSessionManager implements IPlugin, IAsyncEventListener {
    private static final Logger log = LoggerFactory
            .getLogger(MarketSessionManager.class);

    @Autowired
    private ScheduleManager scheduleManager;

    @Autowired
    private IRemoteEventManager eventManager;

    private MarketSessionType currentSessionType;
    private String currentTradeDate;
    private IMarketSession sessionChecker;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected long timerInterval = 1 * 1000;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(MarketSessionRequestEvent.class, null);
            subscribeToEvent(TradeDateRequestEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processMarketSessionRequestEvent(MarketSessionRequestEvent event) {
        Date date = Clock.getInstance().now();
        try {
            MarketSessionData sessionData = sessionChecker.getState(date, null);
            MarketSessionEvent msEvent = new MarketSessionEvent(event.getKey(), event.getSender(), sessionData.getSessionType(),
                    sessionData.getStartDate(), sessionData.getEndDate(), sessionChecker.getTradeDate(), Default.getMarket());
            eventManager.sendLocalOrRemoteEvent(msEvent);
            currentSessionType = sessionData.getSessionType();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processTradeDateRequestEvent(TradeDateRequestEvent event) {
        try {
            String tradeDate = sessionChecker.getTradeDate();
            TradeDateEvent tdEvent = new TradeDateEvent(null, null, tradeDate);
            eventManager.sendEvent(tdEvent);
            this.currentTradeDate = tradeDate;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    public void processPmSettlementEvent(PmSettlementEvent event) {
        log.info("Receive PmSettlementEvent, symbol: " + event.getEvent().getSymbol());
        eventManager.sendEvent(event.getEvent());
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        Date date = Clock.getInstance().now();
        try {
            MarketSessionData sessionData = sessionChecker.getState(date, null);
            checkMarketSession(sessionData);
            checkTradeDate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void checkTradeDate() {
        if (currentTradeDate == null || !currentTradeDate.equals(sessionChecker.getTradeDate())) {
            String tradeDate = sessionChecker.getTradeDate();
            TradeDateEvent tdEvent = new TradeDateEvent(null, null, tradeDate);
            log.info("Send TradeDateEvent: " + tradeDate);
            eventManager.sendEvent(tdEvent);
            currentTradeDate = tradeDate;
        }
    }

    private void checkMarketSession(MarketSessionData sessionData) throws Exception {
        if (currentSessionType == null || !currentSessionType.equals(sessionData.getSessionType())) {
            MarketSessionEvent msEvent = new MarketSessionEvent(null, null, sessionData.getSessionType(),
                    sessionData.getStartDate(), sessionData.getEndDate(), sessionChecker.getTradeDate(), Default.getMarket());
            msEvent.setKey(null);
            msEvent.setReceiver(null);
            log.info("Send MarketSessionEvent: " + msEvent);
            eventManager.sendGlobalEvent(msEvent);
            currentSessionType = sessionData.getSessionType();
        }
    }

    @Override
    public void init() throws Exception {
        log.info("initialising");

        Date date = Clock.getInstance().now();
        sessionChecker.init(date, null);

        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketSessionManager");

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);
    }

    @Override
    public void uninit() {
        eventProcessor.uninit();
    }

    public void onEvent(AsyncEvent event) {
        if (event instanceof MarketSessionEvent) {
            currentSessionType = ((MarketSessionEvent) event).getSession();
            eventManager.sendEvent(event);
        } else {
            log.error("unhandled event: " + event);
        }
    }

    public MarketSessionType getCurrentSessionType() {
        return currentSessionType;
    }

    public void setSessionChecker(MarketSessionChecker sessionChecker) {
        this.sessionChecker = sessionChecker;
    }
}
