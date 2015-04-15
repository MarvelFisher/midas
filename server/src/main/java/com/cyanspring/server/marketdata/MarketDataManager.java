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
package com.cyanspring.server.marketdata;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.adaptor.future.wind.WindFutureDataAdaptor;
import com.cyanspring.common.event.marketdata.*;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.id.Library.Util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.ForexTickTable;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.event.AsyncEventProcessor;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import javax.xml.crypto.Data;

public class MarketDataManager implements IPlugin, IMarketDataListener,
        IMarketDataStateListener {
    private static final Logger log = LoggerFactory
            .getLogger(MarketDataManager.class);
    private static final Logger quoteLog = LoggerFactory
            .getLogger(MarketDataManager.class.getName() + ".QuoteLog");

    private HashMap<String, Quote> quotes = new HashMap<String, Quote>();
    private HashMap<String, DataObject> quoteExtends = new HashMap<String, DataObject>();
    private Map<String, Quote> lastTradeDateQuotes = new HashMap<String, Quote>();
    private Map<String, DataObject> lastTradeDateQuoteExtends = new HashMap<String, DataObject>();

    private ForexTickTable forexTickTable = new ForexTickTable();

    @Autowired
    protected IRemoteEventManager eventManager;

    protected ScheduleManager scheduleManager = new ScheduleManager();

    // private IQuoteChecker quoteChecker = new PriceQuoteChecker();
    private PriceSessionQuoteChecker quoteChecker = new PriceSessionQuoteChecker();

    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected Date lastQuoteSent = Clock.getInstance().now();
    protected long quoteThrottle = 100; // 0 = no throttle
    protected long timerInterval = 300;
    protected Map<String, QuoteEvent> quotesToBeSent = new HashMap<String, QuoteEvent>();
    // private boolean preSubscribed = false;
    // private List<String> preSubscriptionList;
    private List<List<String>> preSubscriptionList = new ArrayList<List<String>>();
    // private IMarketDataAdaptor adaptor;
    private List<IMarketDataAdaptor> adaptors = new ArrayList<IMarketDataAdaptor>();

    private String tickDir = "ticks";
    private String lastQuoteFile = "last.xml";
    private String lastQuoteExtendFile = "lastExtend.xml";
    private String lastTradeDateQuoteFile = "last_tdq.xml";
    private String lastTradeDateQuoteExtendFile = "lastExtend_tdq.xml";
    private long lastQuoteSaveInterval = 20000;
    private Date lastQuoteSaveTime = Clock.getInstance().now();
    private XStream xstream = new XStream(new DomDriver());
    private boolean staleQuotesSent;
    private Date initTime = Clock.getInstance().now();
    private String tradeDate;
    boolean isUninit = false;
    private Map<MarketSessionType, Long> sessionMonitor;
    private Date chkDate;
    private long chkTime;
    boolean state = false;
    private boolean quotePriceWarningIsOpen = false;
    private boolean quoteExtendEventIsSend = true;
    private int quotePriceWarningPercent = 99;
    private boolean quoteLogIsOpen = false;
    private int quoteExtendSegmentSize = 300;

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

    public boolean isQuotePriceWarningIsOpen() {
        return quotePriceWarningIsOpen;
    }

    public int getQuotePriceWarningPercent() {
        return quotePriceWarningPercent;
    }

    public void setQuotePriceWarningIsOpen(boolean quotePriceWarningIsOpen) {
        this.quotePriceWarningIsOpen = quotePriceWarningIsOpen;
    }

    public void setQuotePriceWarningPercent(int quotePriceWarningPercent) {
        this.quotePriceWarningPercent = quotePriceWarningPercent;
    }

    IQuoteAggregator aggregator;

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

    /*
     * int indexAdaptor = 0; void setAdaptorActive(boolean next) { if (next) {
     * indexAdaptor ++; indexAdaptor %= adaptors.size(); }
     *
     * List<IMarketDataAdaptor> list = new
     * ArrayList<IMarketDataAdaptor>(adaptors); for(int i = 0; i < list.size();
     * i++) { IMarketDataAdaptor adaptor = list.get(i); if (indexAdaptor == i) {
     * adaptor.setActive(true); } else { adaptor.setActive(false); } } }
     *
     * void findAdaptorActive() {
     *
     * List<IMarketDataAdaptor> list = new
     * ArrayList<IMarketDataAdaptor>(adaptors); boolean first = false; for(int i
     * = 0; i < list.size(); i++) { IMarketDataAdaptor adaptor = list.get(i); if
     * (first == false && adaptor.getState()){ first = true;
     * adaptor.setActive(true); indexAdaptor = i; } else
     * adaptor.setActive(false); }
     *
     * if (first == false) { indexAdaptor = 0; adaptors.get(0).setActive(true);
     * } }
     */
    private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(QuoteSubEvent.class, null);
            subscribeToEvent(QuoteExtSubEvent.class, null);
            subscribeToEvent(TradeSubEvent.class, null);
            subscribeToEvent(PresubscribeEvent.class, null);
            subscribeToEvent(LastTradeDateQuotesRequestEvent.class, null);
            subscribeToEvent(TradeDateEvent.class, null);
            subscribeToEvent(MarketSessionEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processMarketSessionEvent(MarketSessionEvent event)
            throws Exception {
        if (null != quoteChecker)
            quoteChecker.setSession(event.getSession());
        chkTime = sessionMonitor.get(event.getSession());
        log.info("Get MarketSessionEvent: " + event.getSession()
                + ", map size: " + sessionMonitor.size() + ", checkTime: "
                + chkTime);
        for (IMarketDataAdaptor adapter : adaptors) {
            String adapterName = adapter.getClass().getSimpleName();
            if (adapterName.equals("WindFutureDataAdaptor")) {
                ((com.cyanspring.adaptor.future.wind.WindFutureDataAdaptor) adapter)
                        .processMarketSession(event);
                if (MarketSessionType.PREOPEN == event.getSession()) {
                    log.debug("Process Wind Future PREOPEN resubscribe");
                    ((com.cyanspring.adaptor.future.wind.WindFutureDataAdaptor) adapter)
                            .clearSubscribeMarketData();
                    eventProcessor.onEvent(new PresubscribeEvent(null));
                }
            }
        }

    }

    public void processLastTradeDateQuotesRequestEvent(
            LastTradeDateQuotesRequestEvent event) {
        try {
            if (tradeDate == null) {
                TradeDateRequestEvent tdrEvent = new TradeDateRequestEvent(
                        null, null);
                eventManager.sendEvent(tdrEvent);
            } else {
                List<Quote> lst = new ArrayList<Quote>(
                        lastTradeDateQuotes.values());
                log.info("LastTradeDateQuotesRequestEvent sending lastTradeDateQuotes: "
                        + lst);
                LastTradeDateQuotesEvent lastTDQEvent = new LastTradeDateQuotesEvent(
                        null, null, tradeDate, lst);
                eventManager.sendRemoteEvent(lastTDQEvent);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processTradeDateEvent(TradeDateEvent event) {
        String newTradeDate = event.getTradeDate();
        if (!newTradeDate.equals(tradeDate) || tradeDate == null) {
            tradeDate = newTradeDate;
            try {
                eventManager.sendGlobalEvent(new TradeDateUpdateEvent(null,
                        null, tradeDate));
                saveLastTradeDateQuotes();
                List<Quote> lst = new ArrayList<Quote>(
                        lastTradeDateQuotes.values());
                log.info("LastTradeDatesQuotes: " + lst + ", tradeDate:"
                        + tradeDate);
                eventManager.sendRemoteEvent(new LastTradeDateQuotesEvent(null,
                        null, tradeDate, lst));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void processPresubscribeEvent(PresubscribeEvent event) {
        preSubscribe();
    }

    public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
        log.debug("QuoteSubEvent: " + event.getSymbol() + ", "
				+ event.getReceiver());
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);

        if (quote != null) {
            eventManager.sendLocalOrRemoteEvent(new QuoteEvent(event.getKey(),
                    event.getSender(), quote));
            DataObject quoteExtend = quoteExtends.get(symbol);
            if (isQuoteExtendEventIsSend()) {
                if (quoteExtend != null) {
                    eventManager.sendLocalOrRemoteEvent(new QuoteExtEvent(event.getKey(), event.getSender(), quoteExtend, 1));
                }
            }
        }
        if (quote == null || quote.isStale()) {
            for (int i = 0; i < adaptors.size(); i++) {
                IMarketDataAdaptor adaptor = adaptors.get(i);
                if (!adaptor.getState())
                    continue;
                adaptor.subscribeMarketData(symbol, MarketDataManager.this);
            }
        }
    }

    public void processQuoteExtSubEvent(QuoteExtSubEvent event) throws Exception {
        log.debug("QuoteExtSubEvent:" + event.getKey() + ", "
                + event.getReceiver() + ",tradeDate=" + tradeDate);

        int dataSegmentSize = getQuoteExtendSegmentSize();
		if(dataSegmentSize <= 1) return;

        int transQuoteExtendOffset = 0;
        int totalQuoteExtendCount = 0;

        if (quoteExtends != null && quoteExtends.size() > 0 ) {
            HashMap<String, DataObject> quoteExtendSegmentMap = null;

            for (String symbol : quoteExtends.keySet()) {
                //Check TradeDate
                Date lastTradeDateBySymbol = quoteExtends.get(symbol).get(Date.class, QuoteExtDataField.TIMESTAMP.value());
                if (!tradeDate.equals(DateUtil.formatDate(lastTradeDateBySymbol, "yyyy-MM-dd"))) {
                    continue;
                }
                transQuoteExtendOffset = transQuoteExtendOffset + 1;
                if (transQuoteExtendOffset % dataSegmentSize == 1) {
                    if (transQuoteExtendOffset != 1) {
                        totalQuoteExtendCount = totalQuoteExtendCount + quoteExtendSegmentMap.size();
                        MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                                , quoteExtendSegmentMap, DateUtil.parseDate(tradeDate, "yyyy-MM-dd"));
                        multiQuoteExtendEvent.setOffSet(transQuoteExtendOffset - dataSegmentSize);
                        multiQuoteExtendEvent.setTotalDataCount(-1);
                        eventManager.sendEvent(multiQuoteExtendEvent);
                    }
                    quoteExtendSegmentMap = new HashMap<String, DataObject>();
                }
                quoteExtendSegmentMap.put(symbol, quoteExtends.get(symbol));
            }
            //Check Last Send
			//if count = 0 , send Null Map
			if((quoteExtendSegmentMap != null && quoteExtendSegmentMap.size()>0) || (quoteExtendSegmentMap==null && transQuoteExtendOffset==0)) {
				totalQuoteExtendCount = totalQuoteExtendCount + (quoteExtendSegmentMap != null ? quoteExtendSegmentMap.size() : 0);
				MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
						, quoteExtendSegmentMap, DateUtil.parseDate(tradeDate, "yyyy-MM-dd"));
				if(quoteExtendSegmentMap != null) {
					multiQuoteExtendEvent.setOffSet(
							transQuoteExtendOffset < dataSegmentSize ?
									1 : transQuoteExtendOffset % dataSegmentSize != 0?
									transQuoteExtendOffset - transQuoteExtendOffset % dataSegmentSize + 1: transQuoteExtendOffset - dataSegmentSize + 1);
					multiQuoteExtendEvent.setTotalDataCount(totalQuoteExtendCount);
				}
				eventManager.sendEvent(multiQuoteExtendEvent);
			}
        }
    }


    public void processTradeSubEvent(TradeSubEvent event)
            throws MarketDataException {
        TradeSubEvent tradeSubEvent = (TradeSubEvent) event;
        String symbol = tradeSubEvent.getSymbol();
        Quote quote = quotes.get(symbol);
        if (quote == null) {
            for (int i = 0; i < preSubscriptionList.size(); i++) {
                List<String> preList = preSubscriptionList.get(i);
                if (preList.contains(symbol)) {
                    IMarketDataAdaptor adaptor = adaptors.get(i);
                    adaptor.subscribeMarketData(symbol, MarketDataManager.this);
                }
            }
        }
    }

    private void sendQuoteEvent(QuoteEvent event) {
        try {
            eventManager.sendGlobalEvent(event);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void clearAndSendQuoteEvent(QuoteEvent event) {
        event.getQuote().setTimeSent(Clock.getInstance().now());
        quotesToBeSent.remove(event.getQuote().getSymbol()); // clear anything
        // in queue
        // because we
        // are sending
        // it now
        if (null != aggregator) {
            aggregator.reset(event.getQuote().getSymbol());
        }
        sendQuoteEvent(event);
    }

    private void logStaleInfo(Quote prev, Quote quote, boolean stale) {
        log.info("Quote stale: " + quote.getSymbol() + ", " + stale
                + ", Prev: " + prev + ", New: " + quote);
    }

    // Check Quote Value
    public boolean checkQuote(Quote prev, Quote quote) {
        boolean IsCorrectQuote = true;
        if (prev != null) {
            if (quote.getClose() <= 0) {
                quote.setClose(prev.getClose());
            }
            if (quote.getOpen() <= 0) {
                quote.setOpen(prev.getOpen());
            }
            if (quote.getHigh() <= 0) {
                quote.setHigh(prev.getHigh());
            }
            if (quote.getLow() <= 0) {
                quote.setLow(prev.getLow());
            }
            if (quote.getBid() <= 0) {
                quote.setBid(prev.getBid());
            }
            if (quote.getAsk() <= 0) {
                quote.setAsk(prev.getAsk());
            }
        }

        if (isQuotePriceWarningIsOpen()) {
            if (quote.getClose() > 0 && getQuotePriceWarningPercent() > 0
                    && getQuotePriceWarningPercent() < 100) {
                double preCloseAddWarningPrice = quote.getClose()
                        * (1.0 + getQuotePriceWarningPercent() / 100.0);
                double preCloseSubtractWarningPrice = quote.getClose()
                        * (1.0 - getQuotePriceWarningPercent() / 100.0);
                if (quote.getAsk() > 0
                        && (PriceUtils.GreaterThan(quote.getAsk(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getAsk(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (quote.getBid() > 0
                        && (PriceUtils.GreaterThan(quote.getBid(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getBid(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (quote.getHigh() > 0
                        && (PriceUtils.GreaterThan(quote.getHigh(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getHigh(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (quote.getLow() > 0
                        && (PriceUtils.GreaterThan(quote.getLow(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getLow(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (quote.getOpen() > 0
                        && (PriceUtils.GreaterThan(quote.getOpen(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getOpen(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
            }
        }

        return IsCorrectQuote;
    }

    public void processInnerQuoteEvent(InnerQuoteEvent inEvent) {
        Quote quote = inEvent.getQuote();
        Quote prev = quotes.get(quote.getSymbol());

        if (isQuoteLogIsOpen()) {
            quoteLog.info("Quote Receive : " + "Sc="
                    + inEvent.getSourceId() + ",Symbol=" + quote.getSymbol()
                    + ",A=" + quote.getAsk() + ",B=" + quote.getBid()
                    + ",C=" + quote.getClose() + ",O=" + quote.getOpen()
                    + ",H=" + quote.getHigh() + ",L=" + quote.getLow()
                    + ",Last=" + quote.getLast()
                    + ",Stale=" + quote.isStale() + ",ts="
                    + quote.getTimeStamp().toString() + ",wPcnt="
                    + getQuotePriceWarningPercent()
					+ ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
			);
        }

        if (!checkQuote(prev, quote) && inEvent.getSourceId() <= 100) {
            quoteLog.warn("Quote BBBBB! : " + "Sc=" + inEvent.getSourceId()
                    + ",Symbol=" + quote.getSymbol() + ",A=" + quote.getAsk()
                    + ",B=" + quote.getBid() + ",C=" + quote.getClose()
                    + ",O=" + quote.getOpen() + ",H=" + quote.getHigh()
                    + ",L=" + quote.getLow() + ",Last=" + quote.getLast()
                    + ",Stale=" + quote.isStale()
                    + ",ts=" + quote.getTimeStamp().toString()
                    + ",wPcnt=" + getQuotePriceWarningPercent() + ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
			);
            return;
        }

        if (null == prev) {
            logStaleInfo(prev, quote, quote.isStale());
            quotes.put(quote.getSymbol(), quote);
            clearAndSendQuoteEvent(inEvent.getQuoteEvent());
            return;
        } else if (null != quoteChecker
                && !quoteChecker.checkWithSession(quote)) {
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

    public static String formatDate(Date dt, String strFmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(strFmt);
        return sdf.format(dt);
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        // flush out all quotes throttled
        for (Entry<String, QuoteEvent> entry : quotesToBeSent.entrySet()) {
            sendQuoteEvent(entry.getValue());
            // log.debug("Sending throttle quote: " +
            // entry.getValue().getQuote());
        }
        quotesToBeSent.clear();
        saveLastQuotes();
        broadCastStaleQuotes();
    }

    public void processTradeEvent(TradeEvent event) {
        eventManager.sendEvent(event);
    }

    public MarketDataManager(List<IMarketDataAdaptor> adaptors) {
        // this.adaptor = adaptors.get(0);
        this.adaptors = adaptors;
        // setAdaptorActive(false);
    }

    @Override
    public void init() throws Exception {
        log.info("initialising");
        // subscribe to events
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketDataManager");

        // requestMarketSession
        requestMarketSession();

        // create tick directory
        File file = new File(tickDir);
        if (!file.isDirectory()) {
            log.info("Creating tick directory: " + tickDir);
            if (!file.mkdir()) {
                throw new TickDataException(
                        "Unable to create tick data directory: " + tickDir);
            }
        } else {
            log.info("Existing tick directory: " + tickDir);
        }

        quotes = loadQuotes(tickDir + "/" + lastQuoteFile);
        log.info("Quotes Loaded Counts [" + quotes.size() + "] ");
        for (Entry<String, Quote> entry : quotes.entrySet()) {
            log.info("Quotes Loaded Results [" + entry.getKey() + "] "
                    + entry.getValue().toString());
        }

        quoteExtends = loadExtendQuotes(tickDir + "/" + lastQuoteExtendFile);
        log.info("QuoteExtends Loaded Counts [" + quoteExtends.size() + "] ");
        for (Entry<String, DataObject> entry : quoteExtends.entrySet()) {
            log.info("QuoteExtends Loaded Results [" + entry.getKey() + "] "
                    + entry.getValue().toString());
        }

        lastTradeDateQuotes = loadQuotes(tickDir + "/" + lastTradeDateQuoteFile);
        if (lastTradeDateQuotes == null || lastTradeDateQuotes.size() <= 0) {
            log.warn("No lastTradeDateQuotes values while initialing.");
            lastTradeDateQuotes = (Map<String, Quote>) quotes.clone();
        }

        log.info("LastTradeDateQuotes Loaded Counts ["
                + lastTradeDateQuotes.size() + "] ");
        for (Entry<String, Quote> entry : lastTradeDateQuotes.entrySet()) {
            log.info("LastTradeDateQuotes Loaded Results [" + entry.getKey()
                    + "] " + entry.getValue().toString());
        }

        lastTradeDateQuoteExtends = loadExtendQuotes(tickDir + "/" + lastTradeDateQuoteExtendFile);
        if (lastTradeDateQuoteExtends == null || lastTradeDateQuoteExtends.size() <= 0) {
            log.warn("No lastTradeDateQuoteExtends values while initialing.");
            lastTradeDateQuoteExtends = (Map<String, DataObject>) quoteExtends.clone();
        }

        log.info("LastTradeDateQuoteExtends Loaded Counts ["
                + lastTradeDateQuoteExtends.size() + "] ");
        for (Entry<String, DataObject> entry : lastTradeDateQuoteExtends.entrySet()) {
            log.info("LastTradeDateQuoteExtends Loaded Results [" + entry.getKey()
                    + "] " + entry.getValue().toString());
        }

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

        if (curState) {
            log.debug("Send PreSubScribeEvent...");
            eventProcessor.onEvent(new PresubscribeEvent(null));
        }
        setState(curState);

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    private void saveLastQuotes() {
        if (TimeUtil.getTimePass(lastQuoteSaveTime) < lastQuoteSaveInterval)
            return;

        if (quotes.size() <= 0)
            return;

        lastQuoteSaveTime = Clock.getInstance().now();
        String quoteFileName = tickDir + "/" + lastQuoteFile;
        saveQuotesToFile(quoteFileName, false);
        String quoteExtendFileName = tickDir + "/" + lastQuoteExtendFile;
        saveQuotesToFile(quoteExtendFileName, true);
    }

    private void saveLastTradeDateQuotes() {
        if (lastTradeDateQuotes.size() <= 0 && quotes.size() <= 0)
            return;
        lastTradeDateQuotes = quotes;
        lastTradeDateQuoteExtends = quoteExtends;
        String lastTradeDateQuoteFileName = tickDir + "/" + lastTradeDateQuoteFile;
        saveQuotesToFile(lastTradeDateQuoteFileName, false);
        String lastTradeDateQuoteExtendFileName = tickDir + "/" + lastTradeDateQuoteExtendFile;
        saveQuotesToFile(lastTradeDateQuoteExtendFileName, true);
    }

    private void saveQuotesToFile(String fileName, boolean isExtend) {
        File file = new File(fileName);
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            if (isExtend)
                xstream.toXML(quoteExtends, os);
            else
                xstream.toXML(quotes, os);
            os.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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

    private HashMap<String, DataObject> loadExtendQuotes(String fileName) {
        File file = new File(fileName);
        HashMap<String, DataObject> quoteExtends = new HashMap<>();
        if (file.exists() && quoteExtends.size() <= 0) {
            try {
                ClassLoader save = xstream.getClassLoader();
                ClassLoader cl = HashMap.class.getClassLoader();
                if (cl != null)
                    xstream.setClassLoader(cl);
                quoteExtends = (HashMap<String, DataObject>) xstream.fromXML(file);
                if (!(quoteExtends instanceof HashMap))
                    throw new Exception("Can't xstream load last quote: "
                            + fileName);
                xstream.setClassLoader(save);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info("QuoteExtends loaded: " + fileName);
        }
        return quoteExtends;
    }

    private HashMap<String, Quote> loadQuotes(String fileName) {
        File file = new File(fileName);
        HashMap<String, Quote> quotes = new HashMap<>();
        if (file.exists() && quotes.size() <= 0) {
            try {
                ClassLoader save = xstream.getClassLoader();
                ClassLoader cl = HashMap.class.getClassLoader();
                if (cl != null)
                    xstream.setClassLoader(cl);
                quotes = (HashMap<String, Quote>) xstream.fromXML(file);
                if (!(quotes instanceof HashMap))
                    throw new Exception("Can't xstream load last quote: "
                            + fileName);
                xstream.setClassLoader(save);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            for (Quote quote : quotes.values()) {
                quote.setStale(true);
            }
            log.info("Quotes loaded: " + fileName);
        }
        return quotes;
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
            try {
                eventManager.sendGlobalEvent(event);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
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
            eventProcessor.onEvent(new PresubscribeEvent(null));
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

    private void preSubscribe() {
        if (null == preSubscriptionList)
            return;

        log.debug("Market data presubscribe: " + preSubscriptionList);
        try {
            for (int i = 0; i < preSubscriptionList.size(); i++) {
                List<String> preList = preSubscriptionList.get(i);
                IMarketDataAdaptor adaptor = adaptors.get(i);
                log.debug("Market data presubscribe adapter begin : Adapter=" + adaptor.getClass().getSimpleName() + ",State="
                        + adaptor.getState());
                if (!adaptor.getState())
                    continue;

                for (String symbol : preList) {
                    adaptor.subscribeMarketData(symbol, this);
                }
            }
        } catch (MarketDataException e) {
            log.error(e.getMessage(), e);
        }
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

    public List<List<String>> getPreSubscriptionList() {
        return preSubscriptionList;
    }

    public void setPreSubscriptionList(List<List<String>> preSubscriptionList) {
        this.preSubscriptionList = preSubscriptionList;
    }

    public IQuoteChecker getQuoteChecker() {
        return quoteChecker;
    }

    public void setQuoteChecker(IQuoteChecker quoteChecker) {
        this.quoteChecker = (PriceSessionQuoteChecker) quoteChecker;
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

    public void requestMarketSession() {
        eventManager.sendEvent(new MarketSessionRequestEvent(null, null, true));
    }

    public DataObject getQuoteExtendBySymbol(String symbol) {
        return quoteExtends.get(symbol);
    }

    public HashMap<String, DataObject> getQuoteExtends() {
        return quoteExtends;
    }

}
