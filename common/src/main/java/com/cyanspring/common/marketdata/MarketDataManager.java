package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.marketdata.*;
import com.cyanspring.common.event.marketsession.IndexSessionEvent;
import com.cyanspring.common.event.marketsession.IndexSessionRequestEvent;
import com.cyanspring.common.event.marketsession.MarketSessionRequestEvent;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.RefDataCommodity;
import com.cyanspring.common.util.PriceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private IQuoteSaver quoteSaver;
    private IQuoteCleaner quoteCleaner;
    private IQuoteExtendCleaner quoteExtendCleaner;
    private String tickDir = "ticks";
    private String lastQuoteFile = "last.xml";
    private String lastQuoteExtendFile = "lastExtend.xml";
    private String lastTradeDateQuoteFile = "last_tdq.xml";
    private String lastTradeDateQuoteExtendFile = "lastExtend_tdq.xml";
    private boolean broadcastQuote;
    private volatile boolean isInit = false;
    private RefDataEvent refDataEvent;
    private IndexSessionEvent indexSessionEvent;
    protected HashMap<String, String> tradeDateByIndex = new HashMap<>(); //TradeDateByIndex

    public MarketDataManager(List<IMarketDataAdaptor> adaptors) {
        super(adaptors);
    }

    private List<Class<? extends AsyncEvent>> subscribeEvent() {
        ArrayList<Class<? extends AsyncEvent>> clzList = new ArrayList<Class<? extends AsyncEvent>>();
        clzList.add(TradeSubEvent.class);
        clzList.add(QuoteSubEvent.class);
        clzList.add(QuoteExtSubEvent.class);
        clzList.add(AllQuoteExtSubEvent.class);
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
            log.info("Quotes Loaded Results " + quotes.keySet());

            lastTradeDateQuotes = quoteSaver.loadQuotes(tickDir + "/" + lastTradeDateQuoteFile);
            if (lastTradeDateQuotes == null || lastTradeDateQuotes.size() <= 0) {
                log.warn("No lastTradeDateQuotes values while initialing.");
                lastTradeDateQuotes = (Map<String, Quote>) quotes.clone();
            }

            log.info("LastTradeDateQuotes Loaded Counts ["
                    + lastTradeDateQuotes.size() + "] ");
            log.info("LastTradeDateQuotes Loaded Results " + lastTradeDateQuotes.keySet());

            quoteExtends = quoteSaver.loadExtendQuotes(tickDir + "/" + lastQuoteExtendFile);
            log.info("QuoteExtends Loaded Counts [" + quoteExtends.size() + "] ");
            log.info("QuoteExtends Loaded Results " + quoteExtends.keySet());

            lastTradeDateQuoteExtends = quoteSaver.loadExtendQuotes(tickDir + "/" + lastTradeDateQuoteExtendFile);
            if (lastTradeDateQuoteExtends == null || lastTradeDateQuoteExtends.size() <= 0) {
                log.warn("No lastTradeDateQuoteExtends values while initialing.");
                lastTradeDateQuoteExtends = (Map<String, DataObject>) quoteExtends.clone();
            }

            log.info("LastTradeDateQuoteExtends Loaded Counts ["
                    + lastTradeDateQuoteExtends.size() + "] ");
            log.info("LastTradeDateQuoteExtends Loaded Results " + lastTradeDateQuoteExtends.keySet());
        }

        for (Class<? extends AsyncEvent> clz : subscribeEvent())
            eventProcessor.subscribeToEvent(clz, null);
        super.init();
    }

    public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
        log.debug("QuoteSubEvent: " + event.getSymbol() + ", " + event.getReceiver());
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);

        if (quote != null) {
            quote.setTimeSent(Clock.getInstance().now());
            eventManager.sendLocalOrRemoteEvent(new QuoteEvent(event.getKey(), event.getSender(), quote));
            DataObject quoteExtend = quoteExtends.get(symbol);
            if (isQuoteExtendEventIsSend()) {
                if (quoteExtend != null) {
                    quoteExtend.put(QuoteExtDataField.TIMESENT.value(), Clock.getInstance().now());
                    eventManager.sendLocalOrRemoteEvent(new QuoteExtEvent(event.getKey(), event.getSender(), quoteExtend, QuoteSource.DEFAULT));
                }
            }
        }
        if(isInitReqDataEnd) {
            if (quote == null || quote.isStale()) {
                for (IMarketDataAdaptor adaptor : adaptors) {
                    if (!adaptor.getState())
                        continue;
                    adaptor.subscribeMarketData(symbol, this);
                }
            }
        }
    }

    public void processRefDataEvent(RefDataEvent event) {
        if (event != null && event.isOk() && event.getRefDataList().size() > 0) {
            if (isInit) {
                super.processRefDataEvent(event);
                //Check last.xml Symbol
                log.debug("Process Last.xml Symbol List=" + preSubscriptionList.size());
                quotes.keySet().retainAll(preSubscriptionList);
                quoteExtends.keySet().retainAll(preSubscriptionList);
            } else {
                this.refDataEvent = event;
            }
        }else{
            log.debug("RefData Event NOT OK - " + (event.getRefDataList() != null ? "0" : "null"));
        }
    }

    public void processIndexSessionEvent(IndexSessionEvent event){
        if(event != null && event.isOk() && event.getDataMap().size() > 0) {
            if(isInit) {
                super.processIndexSessionEvent(event);
                for (String index : indexSessions.keySet()) {
                    MarketSessionData marketSessionData = indexSessions.get(index);
                    if (marketSessionData != null) {
                        //Process LastTradeDateQuote
                        if (!marketSessionData.getTradeDateByString().equals(tradeDateByIndex.get(index)) || tradeDateByIndex.get(index) == null) {
                            tradeDateByIndex.put(index, marketSessionData.getTradeDateByString());
                            ArrayList<String> symbols = indexSessionTypes.get(index);
                            log.info("LastTradeDatesQuotes: " + symbols + ", tradeDate:" + tradeDateByIndex.get(index) + ",index:" + index);
                            List<Quote> quoteList = new ArrayList<Quote>();
                            for (String symbol : symbols) {
                                if (quotes.get(symbol) != null) {
                                    quoteList.add((Quote) quotes.get(symbol).clone());
                                }
                            }
                            try {
                                if (quoteList.size() > 0)
                                    eventManager.sendRemoteEvent(new LastTradeDateQuotesEvent(null, null, index, marketSessionData.getTradeDateByString(), quoteList));
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                        //Process Clean Session
                        if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN) {
                            ArrayList<String> symbols = indexSessionTypes.get(index);
                            for (String symbol : symbols) {
                                if (quoteCleaner != null) {
                                    Quote quote = quotes.get(symbol);
                                    if (quote != null) {
                                        Quote tempQuote = (Quote) quote.clone();
                                        if (marketTypes.get(tempQuote.getSymbol()) != null) {
                                            if (RefDataCommodity.INDEX.getValue().equals(marketTypes.get(tempQuote.getSymbol()))
                                                    || RefDataCommodity.STOCK.getValue().equals(marketTypes.get(tempQuote.getSymbol()))) {
                                                tempQuote.setClose(tempQuote.getLast());
                                                log.debug("Symbol=" + tempQuote.getSymbol() + " update preClose = Last = " + tempQuote.getLast());
                                            }
                                            if (RefDataCommodity.FUTURES.getValue().equals(marketTypes.get(tempQuote.getSymbol()))) {
                                                if (quoteExtends.containsKey(tempQuote.getSymbol())) {
                                                    DataObject quoteExtend = quoteExtends.get(tempQuote.getSymbol());
                                                    double settlePrice = tempQuote.getLast();
                                                    if (quoteExtend.fieldExists(QuoteExtDataField.SETTLEPRICE.value()))
                                                        settlePrice = quoteExtend.get(Double.class, QuoteExtDataField.SETTLEPRICE.value());
                                                    tempQuote.setClose(settlePrice);
                                                    log.debug("Symbol=" + tempQuote.getSymbol() + " update preClose = SettlePrice = " + settlePrice);
                                                } else {
                                                    log.debug("Symbol=" + tempQuote.getSymbol() + " not in quoteExtends");
                                                }
                                            }
                                        }
                                        quoteCleaner.clear(tempQuote);
                                        tempQuote.setTimeSent(Clock.getInstance().now());
                                        printQuoteLog(QuoteSource.CLEAN_SESSION, null, tempQuote, QuoteLogLevel.GENERAL);
                                        try {
                                            eventManager.sendRemoteEvent(new QuoteEvent(tempQuote.getSymbol(), null, tempQuote));
                                        } catch (Exception e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    }
                                }
                                if (quoteExtendCleaner != null) {
                                    DataObject quoteExtend = (DataObject) quoteExtends.get(symbol).clone();
                                    if (marketTypes.get(symbol) != null) {
                                        double preClose = 0;
                                        double ceil = 0;
                                        double floor = 0;
                                        if (quotes.containsKey(symbol)) {
                                            preClose = quotes.get(symbol).getLast();
                                        }
                                        if (RefDataCommodity.FUTURES.getValue().equals(marketTypes.get(symbol))) {
                                            if (quoteExtend.fieldExists(QuoteExtDataField.SETTLEPRICE.value())) {
                                                preClose = quoteExtend.get(Double.class, QuoteExtDataField.SETTLEPRICE.value());
                                            }
                                        }
                                        ceil = preClose * 1.1;
                                        floor = preClose * 0.9;
                                        if (PriceUtils.GreaterThan(preClose, 0)) {
                                            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
                                            quoteExtend.put(QuoteExtDataField.CEIL.value(), ceil);
                                            quoteExtend.put(QuoteExtDataField.FLOOR.value(), floor);
                                        }
                                    }
                                    quoteExtendCleaner.clear(quoteExtend);
                                    try {
                                        printQuoteExtendLog(QuoteSource.CLEAN_SESSION, quoteExtend);
                                        quoteExtend.put(QuoteExtDataField.TIMESENT.value(), Clock.getInstance().now());
                                        eventManager.sendRemoteEvent(new QuoteExtEvent(symbol, null, quoteExtend, QuoteSource.CLEAN_SESSION));
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                this.indexSessionEvent = event;
            }
        }else{
            log.debug("IndexSession Event NOT OK - " + (event.getDataMap() != null ? "0" : "null"));
        }
    }

    public void processLastTradeDateQuotesRequestEvent(
            LastTradeDateQuotesRequestEvent event) {
        try {
            if(tradeDateByIndex != null && tradeDateByIndex.size() > 0){
                for(String index : tradeDateByIndex.keySet()){
                    //send LastTradeQuote
                    ArrayList<String> symbols = indexSessionTypes.get(index);
                    log.info("LastTradeDatesQuotes: " + symbols + ", tradeDate:" + tradeDateByIndex.get(index) + ",index:" + index);
                    if(symbols != null && symbols.size() > 0) {
                        List<Quote> quoteList = new ArrayList<Quote>();
                        for (String symbol : symbols) {
                            if (quotes.get(symbol) != null) {
                                quoteList.add((Quote) quotes.get(symbol).clone());
                            }
                        }
                        try {
                            if(quoteList.size()>0) eventManager.sendRemoteEvent(new LastTradeDateQuotesEvent(null, null, index, tradeDateByIndex.get(index), quoteList));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processTradeSubEvent(TradeSubEvent event)
            throws MarketDataException {
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);
        if(isInitReqDataEnd) {
            if (quote == null) {
                for (IMarketDataAdaptor adaptor : adaptors) {
                    if (preSubscriptionList.contains(symbol)) {
                        adaptor.subscribeMarketData(symbol, this);
                    }
                }
            }
        }
    }

    @Override
    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        //check init
        if(!isInit){
            if(refDataEvent != null && indexSessionEvent != null){
                log.debug("MDM Event Process begin");
                isInit = true;
                processRefDataEvent(refDataEvent);
                processIndexSessionEvent(indexSessionEvent);
                log.debug("MDM Event Process end");
            }
        }
        super.processAsyncTimerEvent(event);
        if (quoteSaver != null) {
            quoteSaver.saveLastQuoteToFile(tickDir + "/" + lastQuoteFile, quotes);
            quoteSaver.saveLastQuoteExtendToFile(tickDir + "/" + lastQuoteExtendFile, quoteExtends);
        }
    }

    public void processQuoteExtSubEvent(QuoteExtSubEvent event){
        log.debug("QuoteExtSubEvent:" + event.getKey() + "," + event.getReceiver());
        if(event.getRequestSymbolList() != null && event.getRequestSymbolList().size()>0){
            log.debug("Request Symbol List:" + event.getRequestSymbolList());
            HashMap<String, DataObject> quoteExtendRequest = new HashMap<>();
            for(String symbol : event.getRequestSymbolList()){
                if(quoteExtends.containsKey(symbol)){
                    quoteExtendRequest.put(symbol, (DataObject)quoteExtends.get(symbol).clone());
                }
            }
            if(quoteExtendRequest.size() > 0){
                sendMultiQuoteExtEvent(event, quoteExtendRequest);
            }

        }
    }

    public void processAllQuoteExtSubEvent(AllQuoteExtSubEvent event) throws Exception {
        log.debug("AllQuoteExtSubEvent:" + event.getKey() + ", "
                + event.getReceiver());
        sendMultiQuoteExtEvent(event, quoteExtends);
    }

    public void sendMultiQuoteExtEvent(RemoteAsyncEvent event, HashMap<String, DataObject> quoteExtends){

        int dataSegmentSize = getQuoteExtendSegmentSize();
        if (dataSegmentSize <= 1) return;

        int transQuoteExtendOffset = 0;
        int totalQuoteExtendCount = 0;

        if (quoteExtends != null && quoteExtends.size() > 0) {
            HashMap<String, DataObject> quoteExtendSegmentMap = null;
            for (String symbol : quoteExtends.keySet()) {
                transQuoteExtendOffset = transQuoteExtendOffset + 1;
                if (transQuoteExtendOffset % dataSegmentSize == 1) {
                    if (transQuoteExtendOffset != 1) {
                        totalQuoteExtendCount = totalQuoteExtendCount + quoteExtendSegmentMap.size();
                        MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                                , quoteExtendSegmentMap);
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
            if ((quoteExtendSegmentMap != null && quoteExtendSegmentMap.size() > 0) || (quoteExtendSegmentMap == null && transQuoteExtendOffset == 0)) {
                totalQuoteExtendCount = totalQuoteExtendCount + (quoteExtendSegmentMap != null ? quoteExtendSegmentMap.size() : 0);
                MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                        , quoteExtendSegmentMap);
                if (quoteExtendSegmentMap != null) {
                    multiQuoteExtendEvent.setOffSet(
                            transQuoteExtendOffset < dataSegmentSize ?
                                    1 : transQuoteExtendOffset % dataSegmentSize != 0 ?
                                    transQuoteExtendOffset - transQuoteExtendOffset % dataSegmentSize + 1 : transQuoteExtendOffset - dataSegmentSize + 1);
                    multiQuoteExtendEvent.setTotalDataCount(totalQuoteExtendCount);
                }
                eventManager.sendEvent(multiQuoteExtendEvent);
            }
        }
    }

    @Override
    protected void sendQuoteEvent(RemoteAsyncEvent event) {
        if (isUninit) return;
        try {
            if (broadcastQuote)
                eventManager.sendGlobalEvent(event);
            else
                eventManager.sendEvent(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void requestRequireData() {
        eventManager.sendEvent(new MarketSessionRequestEvent(null, null, true));
        eventManager.sendEvent(new IndexSessionRequestEvent(null, null, null));
        eventManager.sendEvent(new RefDataRequestEvent(null, null));
    }

    public void setQuoteSaver(IQuoteSaver quoteSaver) {
        this.quoteSaver = quoteSaver;
    }

    public void setQuoteCleaner(IQuoteCleaner quoteCleaner) {
        this.quoteCleaner = quoteCleaner;
    }

    public void setQuoteExtendCleaner(IQuoteExtendCleaner quoteExtendCleaner) {
        this.quoteExtendCleaner = quoteExtendCleaner;
    }

    public void setBroadcastQuote(boolean broadcastQuote) {
        this.broadcastQuote = broadcastQuote;
    }

}
