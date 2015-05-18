package com.cyanspring.server.marketsession;

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

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class IndexMarketSessionManager implements IPlugin {
    private static final Logger log = LoggerFactory
            .getLogger(IndexMarketSessionManager.class);

    @Autowired
    private IRemoteEventManager eventManager;

    @Autowired
    private MarketSessionUtil marketSessionUtil;

    private boolean searchBySymbol = true;
    private ScheduleManager scheduleManager = new ScheduleManager();
    private Map<String, MarketSessionData> sessionDataMap;
    private Map<String, RefData> refDataMap;
    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    private Date chkDate;
    private Date tradeDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    protected long timerInterval = 1 * 1000;
    private int settlementDelay = 10;
    private MarketSessionType currentSessionType;

    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(IndexSessionRequestEvent.class, null);
            subscribeToEvent(MarketSessionEvent.class, null);
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
                        marketSessionUtil.getSessionDataBySymbol(refDataList, event.getDate()), true));

            } else {
                eventManager.sendLocalOrRemoteEvent(new IndexSessionEvent(event.getKey(), event.getSender(),
                        marketSessionUtil.getSessionDataByStrategy(event.getIndexList(), event.getDate()), true));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processMarketSessionEvent(MarketSessionEvent event) {
        currentSessionType = event.getSession();
        try {
            tradeDate = sdf.parse(event.getTradeDate());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processRefDataEvent(RefDataEvent event) {
        if (!event.isOk())
            return;
        refDataMap = new HashMap<String, RefData>();
        for (RefData refData : event.getRefDataList()) {
            if (searchBySymbol)
                refDataMap.put(refData.getSymbol(), refData);
            else {
                String index;
                if (refData.getStrategy() != null)
                    index = refData.getStrategy();
                else
                    index = refData.getExchange();
                if (!refDataMap.containsKey(index))
                    refDataMap.put(index, refData);
            }
        }
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
        Date date = Clock.getInstance().now();
        try {
            if (refDataMap == null || refDataMap.size() <= 0)
                return;
            if (sessionDataMap == null) {
                if (searchBySymbol)
                    sessionDataMap = marketSessionUtil.getSessionDataBySymbol(new ArrayList<RefData>(refDataMap.values()), date);
                else
                    sessionDataMap = marketSessionUtil.getSessionDataByStrategy(null, date);

                eventManager.sendGlobalEvent(new IndexSessionEvent(null, null, sessionDataMap, true));
                return;
            }

            Map<String, MarketSessionData> sendMap = new HashMap<String, MarketSessionData>();
            Map<String, MarketSessionData> chkMap;
            if (searchBySymbol)
                chkMap = marketSessionUtil.getSessionDataBySymbol(new ArrayList<RefData>(refDataMap.values()), date);
            else
                chkMap = marketSessionUtil.getSessionDataByStrategy(null, date);

            for (Map.Entry<String, MarketSessionData> entry : chkMap.entrySet()){
                MarketSessionData chkData = sessionDataMap.get(entry.getKey());
                if (chkData == null || !chkData.getSessionType().equals(entry.getValue().getSessionType())){
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
        if (checkRefData())
            return;
        if (TimeUtil.sameDate(chkDate, tradeDate) || !currentSessionType.equals(MarketSessionType.CLOSE))
            return;
        chkDate = tradeDate;
        List<RefData> refDataList = new ArrayList<RefData>(refDataMap.values());
        for (RefData refData : refDataList) {
            if (refData.getSettlementDate() == null)
                continue;
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

        Date date = Clock.getInstance().now();
        chkDate = TimeUtil.getPreviousDay(date);

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
        eventProcessor.uninit();
    }

    public void setSearchBySymbol(boolean searchBySymbol) {
        this.searchBySymbol = searchBySymbol;
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
}
