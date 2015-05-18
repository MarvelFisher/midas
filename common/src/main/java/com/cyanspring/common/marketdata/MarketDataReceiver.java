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
package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketdata.InnerQuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteExtEvent;
import com.cyanspring.common.event.marketdata.TradeEvent;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.event.AsyncEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class MarketDataReceiver implements IPlugin, IMarketDataListener,
        IMarketDataStateListener {
    private static final Logger log = LoggerFactory
            .getLogger(MarketDataReceiver.class);
    private static final Logger quoteLog = LoggerFactory
            .getLogger(MarketDataReceiver.class.getName() + ".QuoteLog");

    protected HashMap<String, Quote> quotes = new HashMap<String, Quote>();
    protected HashMap<String, DataObject> quoteExtends = new HashMap<String, DataObject>();
    protected Map<String, Quote> lastTradeDateQuotes = new HashMap<String, Quote>();
    protected Map<String, DataObject> lastTradeDateQuoteExtends = new HashMap<String, DataObject>();

    @Autowired
    protected IRemoteEventManager eventManager;

    protected ScheduleManager scheduleManager = new ScheduleManager();
    private QuoteChecker quoteChecker;

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected long quoteThrottle = 100; // 0 = no throttle
    protected long timerInterval = 300;
    protected Map<String, QuoteEvent> quotesToBeSent = new HashMap<String, QuoteEvent>();
    protected List<String> preSubscriptionList = new ArrayList<String>();
    protected List<IMarketDataAdaptor> adaptors = new ArrayList<IMarketDataAdaptor>();
    protected String tradeDate;

    private long lastQuoteSaveInterval = 20000;
    private boolean staleQuotesSent;
    private Date initTime = Clock.getInstance().now();
    private Map<MarketSessionType, Long> sessionMonitor;
    private Date chkDate;
    private long chkTime;
    private boolean quoteExtendEventIsSend = true;
    private boolean quoteLogIsOpen = false;
    private int quoteExtendSegmentSize = 300;
    private IQuoteAggregator aggregator;
    private volatile boolean isInitRefDateReceived = false;
    private volatile boolean isInitIndexSessionReceived = false;
    private volatile boolean isInitMarketSessionReceived = false;
    private volatile boolean isInitReqDataEnd = false;
    private volatile boolean isPreSubscribing = false;
    private MarketSessionEvent marketSessionEvent;
    boolean state = false;
    boolean isUninit = false;
    private String serverInfo = null;
    private boolean isTest = false;


    protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {

            subscribeToEvent(MarketSessionEvent.class, null);
            subscribeToEvent(IndexSessionEvent.class, null);
            subscribeToEvent(RefDataEvent.class, null);

            for (Class<? extends AsyncEvent> clz : subscribeEvent()) {
                subscribeToEvent(clz, null);
            }
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    protected List<Class<? extends AsyncEvent>> subscribeEvent() {
        return new ArrayList<Class<? extends AsyncEvent>>();
    }

    public void processMarketSessionEvent(MarketSessionEvent event) throws Exception {
        marketSessionEvent = event; //RecordMarketSession
        if (null != quoteChecker)
            quoteChecker.setSession(event.getSession());
        chkTime = sessionMonitor.get(event.getSession());
        log.info("Get MarketSessionEvent: " + event.getSession()
                + ", map size: " + sessionMonitor.size() + ", checkTime: "
                + chkTime);

        if (aggregator != null) {
            aggregator.onMarketSession(event.getSession());
        }

        for (IMarketDataAdaptor adaptor : adaptors) {
            adaptor.processEvent(event);
        }

        if (!isInitReqDataEnd) isInitMarketSessionReceived = true;
    }

    public void processRefDataEvent(RefDataEvent event) {
        if (isPreSubscribing){
            log.warn("RefData Event coming in presubscribe");
            event = null;
            return;
        }

        if (event.isOk() && event.getRefDataList().size() > 0) {
            log.debug("process RefData Event, Size=" + event.getRefDataList().size());
            preSubscriptionList.clear();
            List refDataList = event.getRefDataList();
            for (int i = 0; i < refDataList.size(); i++) {
                RefData refData = (RefData) refDataList.get(i);
                preSubscriptionList.add(refData.getSymbol());
            }
            for (IMarketDataAdaptor adaptor : adaptors) {
                if (null != adaptor) {
                    adaptor.processEvent(event);
                    if (adaptor.getClass().getSimpleName().equals("WindFutureDataAdaptor")
                            && marketSessionEvent != null && marketSessionEvent.getSession() == MarketSessionType.PREOPEN && isInitReqDataEnd) {
                        adaptor.clean();
                        preSubscribe();
                    }
                }
            }
            if (!isInitReqDataEnd) isInitRefDateReceived = true;
        } else {
            log.debug("RefData Event NOT OK - " + (event.getRefDataList() != null ? "0" : "null"));
        }
    }

    public void processIndexSessionEvent(IndexSessionEvent event) {
        log.debug("Process IndexSession Event");
        if (event.isOk() && event.getDataMap().size() > 0) {
            for (String index : event.getDataMap().keySet()) {
                MarketSessionData marketSessionDate = event.getDataMap().get(index);
                log.debug("Index=" + index + ",SessionType=" + marketSessionDate.getSessionType()
                                + ",Start=" + marketSessionDate.getStart() + ",End=" + marketSessionDate.getEnd()
                );
            }
            for (IMarketDataAdaptor adaptor : adaptors) {
                if (null != adaptor) adaptor.processEvent(event);
            }
            if (!isInitReqDataEnd) isInitIndexSessionReceived = true;
        } else {
            log.debug("IndexSession Event NOT OK - " + (event.getDataMap() != null ? "0" : "null"));
        }
    }

    protected void sendQuoteEvent(RemoteAsyncEvent event) {
        try {
            eventManager.sendEvent(event);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void clearAndSendQuoteEvent(QuoteEvent event) {
        event.getQuote().setTimeSent(Clock.getInstance().now());
        quotesToBeSent.remove(event.getQuote().getSymbol()); // clear anything
        if (null != aggregator) {
            aggregator.reset(event.getQuote().getSymbol());
        }
        Quote quote = event.getQuote();
        if(quoteLogIsOpen) {
            quoteLog.info("Quote Send: "
                            + "Symbol=" + quote.getSymbol()
                            + ",A=" + quote.getAsk() + ",B=" + quote.getBid()
                            + ",C=" + quote.getClose() + ",O=" + quote.getOpen()
                            + ",H=" + quote.getHigh() + ",L=" + quote.getLow()
                            + ",Last=" + quote.getLast()
                            + ",Stale=" + quote.isStale() + ",ts="
                            + quote.getTimeStamp().toString()
                            + ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
            );
        }
        sendQuoteEvent(event);
    }

    private void logStaleInfo(Quote prev, Quote quote, boolean stale) {
        log.info("Quote stale: " + quote.getSymbol() + ", " + stale
                + ", Prev: " + prev + ", New: " + quote);
    }

    public void processInnerQuoteEvent(InnerQuoteEvent inEvent) {
        Quote quote = inEvent.getQuote();
        Quote prev = quotes.get(quote.getSymbol());

        //Calculate Future Quote last Volume
        if (inEvent.getSourceId() > 100) {
            if (prev != null && TimeUtil.formatDate(prev.getTimeStamp(), "yyyy-MM-dd").equals(tradeDate)) {
                quote.setLastVol(quote.getTotalVolume() - prev.getTotalVolume());
            } else {
                quote.setLastVol(quote.getTotalVolume());
            }
        }

        //Check Forex TimeStamp
        if (inEvent.getSourceId() <= 100) {

            if (inEvent.getSourceId() == 2) {
                if (marketSessionEvent != null && (marketSessionEvent.getSession() == MarketSessionType.CLOSE
                        || marketSessionEvent.getSession() == MarketSessionType.PREOPEN)) {
                    return;
                }
            }

            if (marketSessionEvent != null && marketSessionEvent.getSession() == MarketSessionType.OPEN) {
                if (TimeUtil.getTimePass(quote.getTimeStamp(), marketSessionEvent.getEnd()) >= 0) {
                    quote.setTimeStamp(TimeUtil.subDate(marketSessionEvent.getEnd(), 1, TimeUnit.SECONDS));
                }
            }

            quoteChecker.fixPriceQuote(prev, quote);
        }

//        if (isQuoteLogIsOpen()) {
//            quoteLog.info("Quote Receive : " + "Sc="
//                            + inEvent.getSourceId() + ",Symbol=" + quote.getSymbol()
//                            + ",A=" + quote.getAsk() + ",B=" + quote.getBid()
//                            + ",C=" + quote.getClose() + ",O=" + quote.getOpen()
//                            + ",H=" + quote.getHigh() + ",L=" + quote.getLow()
//                            + ",Last=" + quote.getLast()
//                            + ",Stale=" + quote.isStale() + ",ts="
//                            + quote.getTimeStamp().toString()
//                            + ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
//            );
//        }

        if (null != quoteChecker && !quoteChecker.checkQuotePrice(quote) && inEvent.getSourceId() <= 100) {
            quoteLog.warn("Quote BBBBB! : " + "Sc=" + inEvent.getSourceId()
                            + ",Symbol=" + quote.getSymbol() + ",A=" + quote.getAsk()
                            + ",B=" + quote.getBid() + ",C=" + quote.getClose()
                            + ",O=" + quote.getOpen() + ",H=" + quote.getHigh()
                            + ",L=" + quote.getLow() + ",Last=" + quote.getLast()
                            + ",Stale=" + quote.isStale()
                            + ",ts=" + quote.getTimeStamp().toString()
                            + ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
            );
            return;
        }

        if (null == prev) {
            logStaleInfo(prev, quote, quote.isStale());
            quotes.put(quote.getSymbol(), quote);
            clearAndSendQuoteEvent(inEvent.getQuoteEvent());
            return;
        } else if (null != quoteChecker && !quoteChecker.check(quote)) {
            // if wind Adapter Quote always send,if other Adapter Quote prev not
            // stale to send
            if (inEvent.getSourceId() > 100) {
                // Stale continue send Quote
                quotes.put(quote.getSymbol(), quote);
                clearAndSendQuoteEvent(new QuoteEvent(inEvent.getKey(), null,
                        quote));
            } else {
                boolean prevStale = prev.isStale();
                logStaleInfo(prev, quote, true);
                prev.setStale(true); // just set the existing stale
                if (!prevStale) {
                    // Stale send prev Quote
                    clearAndSendQuoteEvent(new QuoteEvent(inEvent.getKey(),
                            null, prev));
                }
            }
            return;
        } else {
            quotes.put(quote.getSymbol(), quote);
            if (prev.isStale() != quote.isStale()) {
                logStaleInfo(prev, quote, quote.isStale());
            }
        }

        String symbol = inEvent.getQuote().getSymbol();

        if (null != aggregator) {
            quote = aggregator.update(symbol, inEvent.getQuote(),
                    inEvent.getSourceId());
        }

        QuoteEvent event = new QuoteEvent(inEvent.getKey(), null, quote);

        if (eventProcessor.isSync()) {
            sendQuoteEvent(event);
            return;
        }

        // queue up quotes
        if (null != prev && quoteThrottle != 0
                && TimeUtil.getTimePass(prev.getTimeSent()) < quoteThrottle) {
            quote.setTimeSent(prev.getTimeSent()); // important record the last
            // time sent of this quote
            quotesToBeSent.put(quote.getSymbol(), event);
            return;
        }

        // send the quote now
        clearAndSendQuoteEvent(event);
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {

        // flush out all quotes throttled
        for (Entry<String, QuoteEvent> entry : quotesToBeSent.entrySet()) {
            sendQuoteEvent(entry.getValue());
        }
        quotesToBeSent.clear();
        broadCastStaleQuotes();
    }

    public void processTradeEvent(TradeEvent event) {
        eventManager.sendEvent(event);
    }

    public MarketDataReceiver(List<IMarketDataAdaptor> adaptors) {
        this.adaptors = adaptors;
    }

    private boolean processInitReqData() {
        if (isInitRefDateReceived && isInitMarketSessionReceived && isInitIndexSessionReceived)
            isInitReqDataEnd = true;
        return isInitReqDataEnd;
    }

    @Override
    public void init() throws Exception {
        log.info("initialising");
        isInitReqDataEnd = false;
        isInitMarketSessionReceived = false;
        isInitRefDateReceived = false;
        isInitIndexSessionReceived = true;
        for (IMarketDataAdaptor adaptor : adaptors) {
            if ("WindFutureDataAdaptor".equals(adaptor.getClass().getSimpleName())) {
                isInitIndexSessionReceived = false;
                break;
            }
        }
        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketDataReceiver");

        requestRequireData();

        chkDate = Clock.getInstance().now();
        for (IMarketDataAdaptor adaptor : adaptors) {
            log.debug("IMarketDataAdaptor=" + adaptor.getClass()
                    + " SubMarketDataState");
            adaptor.subscribeMarketDataState(this);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (final IMarketDataAdaptor adaptor : adaptors) {
                    try {
                        while (!processInitReqData() && !isTest)
                            TimeUnit.SECONDS.sleep(1);
                        log.debug("Adapter:" + adaptor.getClass().getSimpleName() + " init.");
                        adaptor.init();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

        });
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                uninit();
            }
        });

        boolean curState = false;
        for (IMarketDataAdaptor adaptor : adaptors) {
            log.debug(adaptor.getClass() + ", State=" + adaptor.getState());
            if (adaptor.getState())
                curState = true;
        }

//        if (curState) {
//            log.debug("presubscribe quotes...");
//            preSubscribe();
//        }
        setState(curState);

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    private void broadCastStaleQuotes() {
        if (staleQuotesSent)
            return;

        if (TimeUtil.getTimePass(initTime) < lastQuoteSaveInterval)
            return;

        staleQuotesSent = true;
        for (Quote quote : quotes.values()) {
            if (quote.isStale())
                this.clearAndSendQuoteEvent(new QuoteEvent(quote.getSymbol(),
                        null, quote));
        }
    }

    public void reset() {
        quotes.clear();
        quoteExtends.clear();
    }

    @Override
    public void uninit() {
        if (isUninit)
            return;

        isUninit = true;

        log.info("uninitialising");
        if (!eventProcessor.isSync())
            scheduleManager.cancelTimerEvent(timerEvent);

        for (IMarketDataAdaptor adaptor : adaptors) {
            adaptor.uninit();
        }

        eventProcessor.uninit();
    }

    @Override
    public void onQuote(InnerQuote innerQuote) {
        if (TimeUtil.getTimePass(chkDate) > chkTime && chkTime != 0) {
            log.warn("Quotes receive time large than excepted.");
        }

        chkDate = Clock.getInstance().now();
        InnerQuoteEvent event = new InnerQuoteEvent(innerQuote.getSymbol(), null,
                innerQuote.getQuote(), innerQuote.getSourceId());
        eventProcessor.onEvent(event);
    }

    @Override
    public void onQuoteExt(DataObject quoteExt, int sourceId) {

        if (quoteExt != null && isQuoteExtendEventIsSend()) {

            StringBuffer sbQuoteExtendLog = new StringBuffer();
            for (String key : quoteExt.getFields().keySet()) {
                sbQuoteExtendLog.append("," + key + "=" + quoteExt.getFields().get(key));
            }
            quoteLog.info("QuoteExtend Receive : " + "Source=" + sourceId + sbQuoteExtendLog.toString());

            String symbol = quoteExt.get(String.class, QuoteExtDataField.SYMBOL.value());
            quoteExt.put(QuoteExtDataField.TIMESENT.value(), Clock.getInstance().now());
            quoteExtends.put(symbol, quoteExt);
            QuoteExtEvent event = new QuoteExtEvent(quoteExt.get(String.class,
                    QuoteExtDataField.SYMBOL.value()), null, quoteExt, sourceId);
            sendQuoteEvent(event);
        }
    }

    @Override
    public void onTrade(Trade trade) {
        TradeEvent event = new TradeEvent(trade.getSymbol(), null, trade);
        eventProcessor.onEvent(event);
    }

    @Override
    public void onState(boolean on) {
        if (on) {
            log.info("MarketData feed is up");
            setState(true);
            eventManager.sendEvent(new MarketDataReadyEvent(null, true));
            preSubscribe();
        } else {
            for (IMarketDataAdaptor adaptor : adaptors) {
                if (adaptor.getState()) {
                    return;
                }
            }
            log.warn("MarketData feed is down");
            setState(false);
            eventManager.sendEvent(new MarketDataReadyEvent(null, false));
        }
    }

    protected void requestRequireData() throws Exception {
        MarketSessionRequestEvent msEvent = new MarketSessionRequestEvent(null, null);
        IndexSessionRequestEvent isrEvent = new IndexSessionRequestEvent(null, null, null, Clock.getInstance().now());
        RefDataRequestEvent rdrEvent = new RefDataRequestEvent(null, null);
        msEvent.setReceiver(serverInfo);
        isrEvent.setReceiver(serverInfo);
        rdrEvent.setReceiver(serverInfo);
        eventManager.sendRemoteEvent(msEvent);
        eventManager.sendRemoteEvent(isrEvent);
        eventManager.sendRemoteEvent(rdrEvent);
    }

    private void preSubscribe() {
        if (null == preSubscriptionList || preSubscriptionList.size() <= 0)
            return;

        isPreSubscribing = true;
        log.debug("Market data presubscribe: " + preSubscriptionList);
        try {
            for (IMarketDataAdaptor adaptor : adaptors) {
                log.debug("Market data presubscribe adapter begin : Adapter=" + adaptor.getClass().getSimpleName() + ",State="
                        + adaptor.getState());
                if (!adaptor.getState())
                    continue;

                for (String symbol : preSubscriptionList) {
                    adaptor.subscribeMarketData(symbol, this);
                }
            }
        } catch (MarketDataException e) {
            log.error(e.getMessage(), e);
        } finally {
            isPreSubscribing = false;
        }
    }

    public int getQuoteExtendSegmentSize() {
        return quoteExtendSegmentSize;
    }

    public void setQuoteExtendSegmentSize(int quoteExtendSegmentSize) {
        this.quoteExtendSegmentSize = quoteExtendSegmentSize;
    }

    public boolean isQuoteExtendEventIsSend() {
        return quoteExtendEventIsSend;
    }

    public void setQuoteExtendEventIsSend(boolean quoteExtendEventIsSend) {
        this.quoteExtendEventIsSend = quoteExtendEventIsSend;
    }

    public boolean isQuoteLogIsOpen() {
        return quoteLogIsOpen;
    }

    public void setQuoteLogIsOpen(boolean quoteLogIsOpen) {
        this.quoteLogIsOpen = quoteLogIsOpen;
    }

    public IQuoteAggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(IQuoteAggregator aggregator) {
        this.aggregator = aggregator;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isSync() {
        return eventProcessor.isSync();
    }

    public void setSync(boolean sync) {
        eventProcessor.setSync(sync);
    }

    public long getQuoteThrottle() {
        return quoteThrottle;
    }

    public void setQuoteThrottle(long quoteThrottle) {
        this.quoteThrottle = quoteThrottle;
    }

    public List<String> getPreSubscriptionList() {
        return preSubscriptionList;
    }

    public void setPreSubscriptionList(List<String> preSubscriptionList) {
        this.preSubscriptionList = preSubscriptionList;
    }

    public void setSessionMonitor(Map<MarketSessionType, Long> sessionMonitor) {
        this.sessionMonitor = sessionMonitor;
    }

    public long getTimerInterval() {
        return timerInterval;
    }

    public void setTimerInterval(long timerInterval) {
        this.timerInterval = timerInterval;
    }

    public DataObject getQuoteExtendBySymbol(String symbol) {
        return quoteExtends.get(symbol);
    }

    public HashMap<String, DataObject> getQuoteExtends() {
        return quoteExtends;
    }

    public void setQuoteChecker(QuoteChecker quoteChecker) {
        this.quoteChecker = quoteChecker;
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    public void setEventManager(IRemoteEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }
}
