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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.marketsession.*;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;

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

    @Autowired
    private IRefDataManager refDataManager;

    @Autowired(required = false)
    private MarketSessionUtil marketSessionUtil;

    private Date chkDate;
    private int settlementDelay = 10;
    private MarketSessionType currentSessionType;
    private String currentTradeDate;
    private IMarketSession sessionChecker;
    private Map<String, MarketSessionData> sessionDataMap;
    private Map<String, RefData> refDataMap;
    private Map<String, Date> dateMap = new HashMap<String, Date>();
    private boolean searchBySymbol = true;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected long timerInterval = 1 * 1000;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(MarketSessionRequestEvent.class, null);
            subscribeToEvent(TradeDateRequestEvent.class, null);
            subscribeToEvent(IndexSessionRequestEvent.class, null);
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
            MarketSessionEvent msEvent = new MarketSessionEvent(null, null, sessionData.getSessionType(),
                    sessionData.getStartDate(), sessionData.getEndDate(), sessionChecker.getTradeDate(), Default.getMarket());
            msEvent.setKey(null);
            msEvent.setReceiver(null);
            if (event.isLocal())
                eventManager.sendEvent(msEvent);
            else
                eventManager.sendRemoteEvent(msEvent);
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

    public void processIndexSessionRequestEvent(IndexSessionRequestEvent event) {
        if (marketSessionUtil == null)
            return;
        try {
            if (searchBySymbol) {
                List<RefData> refDataList;
                if (event.getIndexList() == null)
                    refDataList = new ArrayList<RefData>(refDataMap.values());
                else {
                    refDataList = new ArrayList<>();
                    for (String index : event.getIndexList()) {
                        refDataList.add(refDataMap.get(index));
                    }
                }

                if (event.getIndexList() != null && refDataList.size() != event.getIndexList().size())
                    log.warn("Not find all refData for IndexSessionRequestEvent, request list: " + event.getIndexList());
                eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(),
                        marketSessionUtil.getSessionDataBySymbol(refDataList, event.getDate())));

            } else {
                eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(),
                        marketSessionUtil.getSessionDataByStrategy(event.getIndexList(), event.getDate())));
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
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
            checkSettlement(date);
            checkIndexMarketSession(date);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void checkIndexMarketSession(Date date) {
        if (marketSessionUtil == null)
            return;
        try {
            if (sessionDataMap == null) {
                if (searchBySymbol)
                    sessionDataMap = marketSessionUtil.getSessionDataBySymbol(refDataManager.getRefDataList(), date);
                else
                    sessionDataMap = marketSessionUtil.getSessionDataByStrategy(null, date);

                for (String key : sessionDataMap.keySet()) {
                    dateMap.put(key, Clock.getInstance().now());
                }
                eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, sessionDataMap));
                return;
            }

            Map<String, MarketSessionData> sendMap = new HashMap<String, MarketSessionData>();
            for (Map.Entry<String, MarketSessionData> entry : sessionDataMap.entrySet()) {
                Date record = dateMap.get(entry.getKey());
                String[] time = entry.getValue().getEnd().split(":");
                Date compare = TimeUtil.getScheduledDate(Calendar.getInstance(), record,
                        Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
                if (date.getTime() < compare.getTime())
                    continue;
                MarketSessionData data = marketSessionUtil.getCurrentMarketSessionType(refDataMap.get(entry.getKey()), date, searchBySymbol);
                sendMap.put(entry.getKey(), data);
                sessionDataMap.put(entry.getKey(), data);
                dateMap.put(entry.getKey(), Clock.getInstance().now());
            }
            if (sendMap.size() > 0)
                eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, sendMap));

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void checkSettlement(Date date) throws ParseException {
        if (refDataManager.getRefDataList().size() <= 0)
            return;
        if (TimeUtil.sameDate(chkDate, date) || !currentSessionType.equals(MarketSessionType.CLOSE))
            return;
        chkDate = date;
        for (RefData refData : refDataManager.getRefDataList()) {
            if (refData.getSettlementDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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

        chkDate = TimeUtil.getPreviousDay(date);

        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketSessionManager");

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval, eventProcessor, timerEvent);

        refDataMap = new HashMap<String, RefData>();
        for (RefData refData : refDataManager.getRefDataList()) {
            if (searchBySymbol)
                refDataMap.put(refData.getSymbol(), refData);
            else {
                if (!refDataMap.containsKey(refData.getStrategy()))
                    refDataMap.put(refData.getStrategy(), refData);
            }
        }
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

    public void setSettlementDelay(int settlementDelay) {
        this.settlementDelay = settlementDelay;
    }

    public void setSearchBySymbol(boolean searchBySymbol) {
        this.searchBySymbol = searchBySymbol;
    }
}
