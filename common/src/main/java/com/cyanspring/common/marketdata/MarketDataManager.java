package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.*;
import com.cyanspring.common.event.marketdata.*;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.event.marketsession.TradeDateRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

/**
 * The manager can collect and broadcast quote data to it's listener.
 * Also manager will save quote/last trade date quote to a file and
 * load it back while initialising.
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class MarketDataManager extends MarketDataReceiver {
    private static final Logger log = LoggerFactory.getLogger(MarketDataManager.class);

    @Autowired(required = false)
    private IRemoteEventManager mdEventManager;
    private AsyncEventProcessor mdProcessor;
    private IQuoteSaver quoteSaver;
    private String tickDir = "ticks";
    private String lastQuoteFile = "last.xml";
    private String lastQuoteExtendFile = "lastExtend.xml";
    private String lastTradeDateQuoteFile = "last_tdq.xml";
    private String lastTradeDateQuoteExtendFile = "lastExtend_tdq.xml";
    private boolean broadcastQuote;

    public MarketDataManager(List<IMarketDataAdaptor> adaptors) {
        super(adaptors);
    }

    private List<Class<? extends AsyncEvent>> subscribeEvent() {
        ArrayList<Class<? extends AsyncEvent>> clzList = new ArrayList<Class<? extends AsyncEvent>>();
        clzList.add(TradeSubEvent.class);
        clzList.add(QuoteExtSubEvent.class);
        clzList.add(QuoteSubEvent.class);
        clzList.add(TradeDateEvent.class);
        clzList.add(LastTradeDateQuotesRequestEvent.class);
        return clzList;
    }

    @Override
    public void init() throws Exception {
        if (quoteSaver != null) {
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

            quotes = quoteSaver.loadQuotes(tickDir + "/" + lastQuoteFile);
            log.info("Quotes Loaded Counts [" + quotes.size() + "] ");
            for (Map.Entry<String, Quote> entry : quotes.entrySet()) {
                log.info("Quotes Loaded Results [" + entry.getKey() + "] "
                        + entry.getValue().toString());
            }

            lastTradeDateQuotes = quoteSaver.loadQuotes(tickDir + "/" + lastTradeDateQuoteFile);
            if (lastTradeDateQuotes == null || lastTradeDateQuotes.size() <= 0) {
                log.warn("No lastTradeDateQuotes values while initialing.");
                lastTradeDateQuotes = (Map<String, Quote>) quotes.clone();
            }

            log.info("LastTradeDateQuotes Loaded Counts ["
                    + lastTradeDateQuotes.size() + "] ");
            for (Map.Entry<String, Quote> entry : lastTradeDateQuotes.entrySet()) {
                log.info("LastTradeDateQuotes Loaded Results [" + entry.getKey()
                        + "] " + entry.getValue().toString());
            }

            quoteExtends = quoteSaver.loadExtendQuotes(tickDir + "/" + lastQuoteExtendFile);
            log.info("QuoteExtends Loaded Counts [" + quoteExtends.size() + "] ");
            for (Map.Entry<String, DataObject> entry : quoteExtends.entrySet()) {
                log.info("QuoteExtends Loaded Results [" + entry.getKey() + "] "
                        + entry.getValue().toString());
            }

            lastTradeDateQuoteExtends = quoteSaver.loadExtendQuotes(tickDir + "/" + lastTradeDateQuoteExtendFile);
            if (lastTradeDateQuoteExtends == null || lastTradeDateQuoteExtends.size() <= 0) {
                log.warn("No lastTradeDateQuoteExtends values while initialing.");
                lastTradeDateQuoteExtends = (Map<String, DataObject>) quoteExtends.clone();
            }

            log.info("LastTradeDateQuoteExtends Loaded Counts ["
                    + lastTradeDateQuoteExtends.size() + "] ");
            for (Map.Entry<String, DataObject> entry : lastTradeDateQuoteExtends.entrySet()) {
                log.info("LastTradeDateQuoteExtends Loaded Results [" + entry.getKey()
                        + "] " + entry.getValue().toString());
            }
        }

        if (mdEventManager == null) {
            mdEventManager = eventManager;
            for (Class<? extends AsyncEvent> clz : subscribeEvent())
                eventProcessor.subscribeToEvent(clz, null);
        } else {
            mdProcessor = new AsyncEventProcessor() {
                @Override
                public void subscribeToEvents() {
                    for (Class<? extends AsyncEvent> clz : subscribeEvent())
                        subscribeToEvent(clz, null);
                }

                @Override
                public IAsyncEventManager getEventManager() {
                    return mdEventManager;
                }
            };

            try {
                mdProcessor.setHandler(this);
                mdProcessor.init();
                if (mdProcessor.getThread() != null)
                    mdProcessor.getThread().setName("MarketDataManager");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        super.init();
    }


    public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
        log.debug("QuoteSubEvent: " + event.getSymbol() + ", " + event.getReceiver());
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);

        if (quote != null) {
            mdEventManager.sendLocalOrRemoteEvent(new QuoteEvent(event.getKey(), event.getSender(), quote));
            DataObject quoteExtend = quoteExtends.get(symbol);
            if (isQuoteExtendEventIsSend()) {
                if (quoteExtend != null) {
                    mdEventManager.sendLocalOrRemoteEvent(new QuoteExtEvent(event.getKey(), event.getSender(), quoteExtend, 1));
                }
            }
        }
        if (quote == null || quote.isStale()) {
            for (IMarketDataAdaptor adaptor : adaptors) {
                if (!adaptor.getState())
                    continue;
                adaptor.subscribeMarketData(symbol, this);
            }
        }
    }

    public void processRefDataEvent(RefDataEvent event){
        super.processRefDataEvent(event);
        //Check last.xml Symbol
        if(event != null && event.isOk() && event.getRefDataList().size() > 0){
            log.debug("Process Last.xml Symbol List=" + preSubscriptionList.size());
            quotes.keySet().retainAll(preSubscriptionList);
            quoteExtends.keySet().retainAll(preSubscriptionList);
        }
    }

    public void processLastTradeDateQuotesRequestEvent(
            LastTradeDateQuotesRequestEvent event) {
        try {
            if (tradeDate == null) {
                TradeDateRequestEvent tdrEvent = new TradeDateRequestEvent(
                        null, null);
                mdEventManager.sendEvent(tdrEvent);
            } else {
                List<Quote> lst = new ArrayList<Quote>(
                        lastTradeDateQuotes.values());
                log.info("LastTradeDateQuotesRequestEvent sending lastTradeDateQuotes: "
                        + lst);
                LastTradeDateQuotesEvent lastTDQEvent = new LastTradeDateQuotesEvent(
                        null, null, tradeDate, lst);
                mdEventManager.sendRemoteEvent(lastTDQEvent);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processTradeSubEvent(TradeSubEvent event)
            throws MarketDataException {
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);
        if (quote == null) {
            for (IMarketDataAdaptor adaptor : adaptors) {
                if (preSubscriptionList.contains(symbol)) {
                    adaptor.subscribeMarketData(symbol, this);
                }
            }
        }
    }

    @Override
    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        super.processAsyncTimerEvent(event);
        if (quoteSaver != null) {
            quoteSaver.saveLastQuoteToFile(tickDir + "/" + lastQuoteFile, quotes);
            quoteSaver.saveLastQuoteExtendToFile(tickDir + "/" + lastQuoteExtendFile, quoteExtends);
        }
    }

    public void processTradeDateEvent(TradeDateEvent event) {
        String newTradeDate = event.getTradeDate();
        if (tradeDate == null || !newTradeDate.equals(tradeDate)) {
            tradeDate = newTradeDate;
            try {
                List<Quote> lst = new ArrayList<Quote>(lastTradeDateQuotes.values());
                log.info("LastTradeDatesQuotes: " + lst + ", tradeDate:" + tradeDate);
                if (quoteSaver != null) {
                    quoteSaver.saveLastTradeDateQuoteToFile(tickDir + "/" + lastTradeDateQuoteFile, quotes, lastTradeDateQuotes);
                    quoteSaver.saveLastTradeDateQuoteExtendToFile(tickDir + "/" + lastTradeDateQuoteExtendFile, quoteExtends, lastTradeDateQuoteExtends);
                }
                mdEventManager.sendRemoteEvent(new LastTradeDateQuotesEvent(null, null, tradeDate, lst));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void processQuoteExtSubEvent(QuoteExtSubEvent event) throws Exception {
        log.debug("QuoteExtSubEvent:" + event.getKey() + ", "
                + event.getReceiver() + ",tradeDate=" + tradeDate);

        int dataSegmentSize = getQuoteExtendSegmentSize();
        if (dataSegmentSize <= 1) return;

        int transQuoteExtendOffset = 0;
        int totalQuoteExtendCount = 0;

        if (quoteExtends != null && quoteExtends.size() > 0) {
            HashMap<String, DataObject> quoteExtendSegmentMap = null;

            for (String symbol : quoteExtends.keySet()) {
                //Check TradeDate
                Date lastTradeDateBySymbol = quoteExtends.get(symbol).get(Date.class, QuoteExtDataField.TIMESTAMP.value());
                if (!tradeDate.equals(TimeUtil.formatDate(lastTradeDateBySymbol, "yyyy-MM-dd"))) {
                    continue;
                }
                transQuoteExtendOffset = transQuoteExtendOffset + 1;
                if (transQuoteExtendOffset % dataSegmentSize == 1) {
                    if (transQuoteExtendOffset != 1) {
                        totalQuoteExtendCount = totalQuoteExtendCount + quoteExtendSegmentMap.size();
                        MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                                , quoteExtendSegmentMap, TimeUtil.parseDate(tradeDate, "yyyy-MM-dd"));
                        multiQuoteExtendEvent.setOffSet(transQuoteExtendOffset - dataSegmentSize);
                        multiQuoteExtendEvent.setTotalDataCount(-1);
                        mdEventManager.sendEvent(multiQuoteExtendEvent);
                    }
                    quoteExtendSegmentMap = new HashMap<String, DataObject>();
                }
                quoteExtendSegmentMap.put(symbol, quoteExtends.get(symbol));
            }
            //Check Last Send
            //if count = 0 , send Null Map
            if ((quoteExtendSegmentMap != null && quoteExtendSegmentMap.size() > 0) || (quoteExtendSegmentMap == null && transQuoteExtendOffset == 0)) {
                totalQuoteExtendCount = totalQuoteExtendCount + (quoteExtendSegmentMap != null ? quoteExtendSegmentMap.size() : 0);
                MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                        , quoteExtendSegmentMap, TimeUtil.parseDate(tradeDate, "yyyy-MM-dd"));
                if (quoteExtendSegmentMap != null) {
                    multiQuoteExtendEvent.setOffSet(
                            transQuoteExtendOffset < dataSegmentSize ?
                                    1 : transQuoteExtendOffset % dataSegmentSize != 0 ?
                                    transQuoteExtendOffset - transQuoteExtendOffset % dataSegmentSize + 1 : transQuoteExtendOffset - dataSegmentSize + 1);
                    multiQuoteExtendEvent.setTotalDataCount(totalQuoteExtendCount);
                }
                mdEventManager.sendEvent(multiQuoteExtendEvent);
            }
        }
    }

    @Override
    protected void sendQuoteEvent(RemoteAsyncEvent event) {
        if(isUninit) return;
        try {
            if (broadcastQuote)
                mdEventManager.sendGlobalEvent(event);
            else
                mdEventManager.sendEvent(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void requestRequireData() {
        eventManager.sendEvent(new MarketSessionRequestEvent(null, null, true));
        eventManager.sendEvent(new IndexSessionRequestEvent(null, null, null, Clock.getInstance().now()));
        eventManager.sendEvent(new RefDataRequestEvent(null, null));
    }

    public void setQuoteSaver(IQuoteSaver quoteSaver) {
        this.quoteSaver = quoteSaver;
    }

    public void setBroadcastQuote(boolean broadcastQuote) {
        this.broadcastQuote = broadcastQuote;
    }
}
