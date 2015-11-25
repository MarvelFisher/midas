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
import com.cyanspring.common.event.marketsession.*;
import com.cyanspring.common.event.refdata.RefDataEvent;
import com.cyanspring.common.event.refdata.RefDataRequestEvent;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.server.event.MarketDataReadyEvent;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
    protected HashMap<String, Long> instrumentTypes = new HashMap<>();
    protected ConcurrentHashMap<String, MarketSessionData> indexSessions = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, ArrayList<String>> indexSessionTypes = new ConcurrentHashMap<String, ArrayList<String>>(); //SymoblArrryByIndex
    private ConcurrentHashMap<String, String> indexs = new ConcurrentHashMap<>(); //IndexBySymbol

    @Autowired
    protected IRemoteEventManager eventManager;

    protected ScheduleManager scheduleManager = new ScheduleManager();
    private QuoteChecker quoteChecker;
    protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
    protected long quoteThrottle = 100; // 0 = no throttle
    protected long timerInterval = 300;
    protected ConcurrentHashMap<String, InnerQuoteEvent> innerQuotesToBeSent = new ConcurrentHashMap<String, InnerQuoteEvent>();
    protected List<String> preSubscriptionList = new ArrayList<String>();
    protected List<IMarketDataAdaptor> adaptors = new ArrayList<IMarketDataAdaptor>();
    protected List<IAsyncEventListener> eventListeners = new ArrayList<>();
    private Date chkDate;
    private long chkTime;
    private boolean quoteExtendEventIsSend = true;
    private boolean quoteLogIsOpen = false;
    private int quoteExtendSegmentSize = 300;
    private IQuoteAggregator aggregator;
    private volatile boolean isInitRefDateReceived = false;
    private volatile boolean isInitIndexSessionReceived = false;
    private volatile boolean isInitMarketSessionReceived = false;
    protected volatile boolean isInit = false;
    protected volatile boolean isInitReqDataEnd = false;
    protected volatile boolean isPreSubscribing = false;
    protected RefDataEvent refDataEvent;
    protected IndexSessionEvent indexSessionEvent;
    boolean state = false;
    boolean isUninit = false;
    private String serverInfo = null;
    private boolean nonWait = false;
    protected String requestDataEventkey;
    private IQuoteListener quoteListener;

    protected AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

        @Override
        public void subscribeToEvents() {
            subscribeToEvent(IndexSessionEvent.class, null);
            subscribeToEvent(RefDataEvent.class, null);
        }

        @Override
        public IAsyncEventManager getEventManager() {
            return eventManager;
        }
    };

    public void processRefDataEvent(RefDataEvent event) {
        if(event != null && (event.getKey() != null && !event.getKey().equals(requestDataEventkey))){
            log.debug("refData event Key not send self:" + event.getKey());
            return;
        }
        if (isPreSubscribing) {
            log.warn("RefData Event coming in presubscribe");
            event = null;
            return;
        }

        if (event.isOk() && event.getRefDataList().size() > 0) {
            if(isInit) {
                log.debug("process RefData Event, Size=" + event.getRefDataList().size() + ",Key=" + event.getKey());
//                preSubscriptionList.clear();
                List<RefData> refDataList = event.getRefDataList();
                List<String> newSubscribeList = new ArrayList<String>();
                for (int i = 0; i < refDataList.size(); i++) {
                    RefData refData = (RefData) refDataList.get(i);
                    if(preSubscriptionList.size()==0) {
                        preSubscriptionList.add(refData.getSymbol());
                    }else{
                        //not initial refData event coming, check refdata symbol have new symbol must subscribe.
                        if(!preSubscriptionList.contains(refData.getSymbol())){
                            preSubscriptionList.add(refData.getSymbol());
                            newSubscribeList.add(refData.getSymbol());
                        }
                    }
                    instrumentTypes.put(refData.getSymbol(), refData.getInstrumentType());
                    if(!checkIndexSessionType(refData)) continue;
                }
                checkEventAndSend(event);
                if(newSubscribeList.size() > 0){
                    for(IMarketDataAdaptor adaptor : adaptors){
                        if(adaptor.getState()) try {
                            adaptor.subscribeMultiMarketData(newSubscribeList, this);
                        } catch (MarketDataException e) {
                            log.error("newSubscribe:" + e.getMessage(), e);
                        }
                    }
                }
                if (!isInitReqDataEnd) isInitRefDateReceived = true;
            }else{
                refDataEvent = event;
            }
        } else {
            log.debug("RefData Event NOT OK - " + (event.getRefDataList() != null ? "0" : "null"));
        }
    }

    public boolean checkIndexSessionType(RefData refData){
        String indexSessionType = refData.getIndexSessionType();
        String index = "";
        switch (indexSessionType) {
            case "SPOT":
                index = refData.getCategory();
                break;
            case "SETTLEMENT":
                index = refData.getSymbol();
                break;
            case "EXCHANGE":
                index = refData.getExchange();
                break;
            default:
                break;
        }
        if(index == null || index.equals("")) return false;
        //if symbol this indexSessionType is Settlement,and prev indexSessionType is Exchange/Category,
        //then delete old indexSessionTypes value symbol
        //if symbol this indexSessionType is Exchange/Category,and prev indexSessionTypes is Settlement
        //then delete indexSessionTypes index key
        if(indexs.get(refData.getSymbol()) != null && !"".equals(indexs.get(refData.getSymbol()))){
            String prevIndex = indexs.get(refData.getSymbol());
            if("SETTLEMENT".equals(indexSessionType)){
                if(!prevIndex.equals(index)) {
                    ArrayList<String> prevSymbols = indexSessionTypes.get(prevIndex);
                    prevSymbols.remove(refData.getSymbol());
                }
            }else{
                if(prevIndex.equals(refData.getSymbol())){
                    if(indexSessionTypes.containsKey(refData.getSymbol())){
                        indexSessionTypes.remove(refData.getSymbol());
                    }
                    if(indexSessions.containsKey(refData.getSymbol())){
                        indexSessions.remove(refData.getSymbol());
                    }
                }
            }
        }
        ArrayList<String> symbols = null;
        if (indexSessionTypes.containsKey(index)) {
            symbols = indexSessionTypes.get(index);
            if(!symbols.contains(refData.getSymbol())) symbols.add(refData.getSymbol());
        } else {
            symbols = new ArrayList<String>();
            symbols.add(refData.getSymbol());
        }
        if (symbols != null) indexSessionTypes.put(index, symbols);

        indexs.put(refData.getSymbol(),index);
        return true;
    }

    public void processIndexSessionEvent(IndexSessionEvent event) {
        log.debug("Process IndexSession Event");
        if (event.isOk() && event.getDataMap().size() > 0) {
            if(isInit) {
                for (String index : event.getDataMap().keySet()) {
                    MarketSessionData marketSessionData = event.getDataMap().get(index);
                    log.info("Index=" + index + ",tradeDate=" + marketSessionData.getTradeDateByString()
                                    + ",SessionType=" + marketSessionData.getSessionType()
                                    + ",Start=" + marketSessionData.getStart() + ",End=" + marketSessionData.getEnd()
                    );
                    indexSessions.put(index, marketSessionData);
                }
                checkEventAndSend(event);
                if (!isInitReqDataEnd) isInitIndexSessionReceived = true;
            }else{
                indexSessionEvent = event;
            }
        } else {
            log.debug("IndexSession Event NOT OK - " + (event.getDataMap() != null ? "0" : "null"));
        }
    }

    protected void sendQuoteEvent(RemoteAsyncEvent event) {
        if (isUninit) return;
        try {
            eventManager.sendEvent(event);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void clearAndSendQuoteEvent(QuoteSource quoteSource, String contributor, QuoteEvent event) {

        event.getQuote().setTimeSent(Clock.getInstance().now());
        if (quoteLogIsOpen)
            printQuoteLog(quoteSource, contributor, event.getQuote(), QuoteLogLevel.GENERAL);

        innerQuotesToBeSent.remove(event.getQuote().getSymbol()); // clear anything

        if (null != aggregator) {
            aggregator.reset(event.getQuote().getSymbol());
        }
        sendQuoteEvent(event);
    }

    public void processInnerQuoteEvent(InnerQuoteEvent inEvent) throws ParseException {
        Quote quote = inEvent.getQuote();
        Quote prev = quotes.get(quote.getSymbol());

        //Calculate Future Quote last Volume
        if (inEvent.getQuoteSource() == QuoteSource.WIND_GENERAL
                || inEvent.getQuoteSource() == QuoteSource.WIND_INDEX) {
            if (prev != null && PriceUtils.GreaterThan(quote.getTotalVolume(), prev.getTotalVolume())) {
                quote.setLastVol(quote.getTotalVolume() - prev.getTotalVolume());
            } else {
                quote.setLastVol(0);
            }
        }

        //Check Forex TimeStamp
        if (inEvent.getQuoteSource()==QuoteSource.ID || inEvent.getQuoteSource()==QuoteSource.IB) {
            MarketSessionData marketSessionData = null;
            try{
                marketSessionData = indexSessions.get(indexs.get(quote.getSymbol()));
            }catch (Exception e){
                log.debug("forex can't get marketSessionData - " + quote.getSymbol() + "," + e.getMessage());
            }
            if (marketSessionData != null && (marketSessionData.getSessionType() == MarketSessionType.CLOSE
                    || marketSessionData.getSessionType() == MarketSessionType.PREMARKET)) {
                if(quotes.containsKey(quote.getSymbol())){
                    //get IB close & Open price
                    if(inEvent.getQuoteSource()==QuoteSource.IB){
                        Quote tmpQuote = quotes.get(quote.getSymbol());
                        if(PriceUtils.GreaterThan(quote.getClose(), 0)) tmpQuote.setClose(quote.getClose());
                        if(PriceUtils.GreaterThan(quote.getOpen(), 0)) tmpQuote.setOpen(quote.getOpen());
                    }
                } else {
                	quote.setStale(true);
                    quotes.put(quote.getSymbol(), quote);
                }
                return;
            }
            if(null != quoteChecker && !quoteChecker.checkBidAskPirce(quote)) return;
            if (marketSessionData != null && marketSessionData.getSessionType() == MarketSessionType.OPEN) {
                if (TimeUtil.getTimePass(quote.getTimeStamp(), marketSessionData.getEndDate()) >= 0) {
                    quote.setTimeStamp(TimeUtil.subDate(marketSessionData.getEndDate(), 1, TimeUnit.SECONDS));
                }
            }
            if (null != quoteChecker) quoteChecker.fixPriceQuote(prev, quote);
            if (null != quoteChecker && !quoteChecker.checkQuotePrice(quote)) {
                printQuoteLog(inEvent.getQuoteSource(), inEvent.getContributor(), quote, QuoteLogLevel.PRICE_ERROR);
                return;
            }
            quote.setLast((quote.getBid()+quote.getAsk())/2.0); //set forex lastprice
        }
        if (null == prev) {
            quotes.put(quote.getSymbol(), quote);
            clearAndSendQuoteEvent(inEvent.getQuoteSource(), inEvent.getContributor(), inEvent.getQuoteEvent());
            log.debug("Prev is null,S=" + quote.getSymbol());
            return;
        } else {
            quotes.put(quote.getSymbol(), quote);
        }

        String symbol = inEvent.getQuote().getSymbol();

        if (null != aggregator) {
            quote = aggregator.update(symbol, inEvent.getQuote(),
                    inEvent.getQuoteSource());
        }

        QuoteEvent event = new QuoteEvent(inEvent.getKey(), null, quote);

    	if (quoteListener != null)
    		quoteListener.onQuote(quote);
    	
        if (eventProcessor.isSync()) {
            sendQuoteEvent(event);
            return;
        }

        // queue up quotes
        if (null != prev && quoteThrottle != 0
                && TimeUtil.getTimePass(prev.getTimeSent()) < quoteThrottle) {
            quote.setTimeSent(prev.getTimeSent()); // important record the last
            // time sent of this quote
            innerQuotesToBeSent.put(quote.getSymbol(),
                    new InnerQuoteEvent(null, null, event.getQuote(), inEvent.getQuoteSource(), inEvent.getContributor()));
            return;
        }

        // send the quote now
        clearAndSendQuoteEvent(inEvent.getQuoteSource(), inEvent.getContributor(), event);
    }

    public void printQuoteLog(QuoteSource quoteSource, String contributor, Quote quote, QuoteLogLevel quoteLogLevel) {
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        sb.append("Sc=" + quoteSource.getValue()
                        + ",S=" + quote.getSymbol() + ",A=" + quote.getAsk()
                        + ",B=" + quote.getBid() + ",C=" + quote.getClose()
                        + ",O=" + quote.getOpen() + ",H=" + quote.getHigh()
                        + ",L=" + quote.getLow() + ",Last=" + quote.getLast()
                        + ",Stale=" + quote.isStale() + ",tO=" + quote.getTurnover()
                        + ",ts=" + sdf.format(quote.getTimeStamp())
                        + ",tt=" + sdf.format(quote.getTimeSent())
                        + ",lsV=" + quote.getLastVol() + ",tV=" + quote.getTotalVolume()
                        + (quoteSource == QuoteSource.ID ? ",Cb=" + contributor : "")
        );
        if (quoteLogLevel == QuoteLogLevel.GENERAL) quoteLog.debug("Quote Receive : " + sb.toString());
        if (quoteLogLevel == QuoteLogLevel.TIME_ERROR) quoteLog.warn("Quote Time BBBBB!:" + sb.toString());
        if (quoteLogLevel == QuoteLogLevel.PRICE_ERROR) quoteLog.error("Quote Price BBBBB!:" + sb.toString());

    }

    public void printQuoteExtendLog(QuoteSource quoteSource, DataObject quoteExtend){
        StringBuffer sbQuoteExtendLog = new StringBuffer();
        for (String key : quoteExtend.getFields().keySet()) {
            sbQuoteExtendLog.append("," + (key.length() >= 3 ? key.substring(0, 2) + key.substring(key.length() - 1, key.length()) : key) + "=" + quoteExtend.getFields().get(key));
        }
        if(quoteLogIsOpen) quoteLog.debug("QuoteExtend Receive : " + "Sc=" + quoteSource.getValue() + sbQuoteExtendLog.toString());
    }

    public void processAsyncTimerEvent(AsyncTimerEvent event) {
        //Check init
        if(!isInit) {
            if (refDataEvent != null && indexSessionEvent != null) {
                log.debug("MDR Event Process begin");
                isInit = true;
                processRefDataEvent(refDataEvent);
                processIndexSessionEvent(indexSessionEvent);
                log.debug("MDR Event Process end");
            }
        }
        // flush out all quotes throttled
        for (Entry<String, InnerQuoteEvent> entry : innerQuotesToBeSent.entrySet()) {
            InnerQuoteEvent innerQuoteEvent = entry.getValue();
            if(quoteLogIsOpen) printQuoteLog(innerQuoteEvent.getQuoteSource(),innerQuoteEvent.getContributor()
                    ,innerQuoteEvent.getQuote(),QuoteLogLevel.GENERAL);
            sendQuoteEvent(innerQuoteEvent.getQuoteEvent());
        }
        innerQuotesToBeSent.clear();
        broadCastStaleQuotes();
    }

    public void processTradeEvent(TradeEvent event) {
        eventManager.sendEvent(event);
    }

    public MarketDataReceiver(List<IMarketDataAdaptor> adaptors) {
        this.adaptors = adaptors;
    }

    private boolean processInitReqData() {
        if ((isInitRefDateReceived && isInitMarketSessionReceived && isInitIndexSessionReceived) || nonWait)
            isInitReqDataEnd = true;
        return isInitReqDataEnd;
    }

    @Override
    public void init() throws Exception {
        log.info("initialising");
        log.info("quoteThrottle=" + quoteThrottle);
        if(requestDataEventkey == null || "".equals(requestDataEventkey))
            requestDataEventkey = IdGenerator.getInstance().getNextID();
        log.info("requestDataEventkey:" + requestDataEventkey);
        isUninit = false;
        isInitReqDataEnd = false;
        isInitRefDateReceived = false;
        isInitIndexSessionReceived = false;
        isInitMarketSessionReceived = true;
        // subscribe to events
        if (quoteListener != null)
        	quoteListener.init();
        log.info("eventProcessor init begin -" + (eventProcessor.getEventManager() == null?"null":"ok"));
        eventProcessor.setHandler(this);
        eventProcessor.init();
        if (eventProcessor.getThread() != null)
            eventProcessor.getThread().setName("MarketDataReceiver");
        log.info("eventProcessor init end -" + (eventProcessor.getEventManager() == null?"null":"ok"));

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
                        while (!processInitReqData())
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

        setState(curState);

        if (!eventProcessor.isSync())
            scheduleManager.scheduleRepeatTimerEvent(timerInterval,
                    eventProcessor, timerEvent);
    }

    private void broadCastStaleQuotes() {
        for(String index : indexSessions.keySet()){
            MarketSessionData marketSessionData= indexSessions.get(index);
            if(marketSessionData != null && (marketSessionData.getSessionType() == MarketSessionType.CLOSE
                || marketSessionData.getSessionType() == MarketSessionType.BREAK ))
            {
                ArrayList<String> symbols = indexSessionTypes.get(index);
                if(symbols == null || symbols.size() == 0){
                    log.debug("indexSessionType index:" + index +" list empty.");
                    continue;
                }
                for(String symbol : symbols){
                    Quote quote = quotes.get(symbol);
                    if (quote != null && !quote.isStale()) {
                        log.debug("MDR send final stale quote event:" + quote.getSymbol());
                        quote.setStale(true);
                        clearAndSendQuoteEvent(QuoteSource.RESEND, null, new QuoteEvent(quote.getSymbol(), null, quote));
                    }
                }
            }
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
        if (!eventProcessor.isSync()) {
            scheduleManager.uninit();
        }

        for (IMarketDataAdaptor adaptor : adaptors) {
        	adaptor.unsubscribeMarketDataState(this);
            adaptor.uninit();
        }

        eventProcessor.uninit();
    }

    @Override
    public void onQuote(InnerQuote innerQuote) {
        if (TimeUtil.getTimePass(chkDate) > chkTime && chkTime != 0) {
            log.warn("Quotes receive time large than excepted.");
        }
        if(quoteChecker != null) {
            if (!quoteChecker.checkTickTime(innerQuote.getQuote(), innerQuote.getThrowQuoteTimeInterval())) {
                printQuoteLog(innerQuote.getQuoteSource(), innerQuote.getContributor(), innerQuote.getQuote(), QuoteLogLevel.TIME_ERROR);
                return;
            }
        }
        chkDate = Clock.getInstance().now();
        InnerQuoteEvent event = new InnerQuoteEvent(innerQuote.getSymbol(), null,
                innerQuote.getQuote(), innerQuote.getQuoteSource(), innerQuote.getContributor());
        eventProcessor.onEvent(event);
    }

    @Override
    public void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource) {
    	if (quoteListener != null)
    		quoteListener.onQuoteExt(quoteExt, quoteSource);
        if (quoteExt != null && isQuoteExtendEventIsSend()) {
            printQuoteExtendLog(quoteSource, quoteExt);
            String symbol = quoteExt.get(String.class, QuoteExtDataField.SYMBOL.value());
            quoteExt.put(QuoteExtDataField.TIMESENT.value(), Clock.getInstance().now());
            if(quoteExtends.containsKey(symbol)) {
                DataObject quoteExtTmp = quoteExtends.get(symbol);
                quoteExtTmp.update(quoteExt);
            }else{
                quoteExtends.put(symbol, quoteExt);
            }
            QuoteExtEvent event = new QuoteExtEvent(quoteExt.get(String.class,
                    QuoteExtDataField.SYMBOL.value()), null, quoteExt, quoteSource);
            sendQuoteEvent(event);
        }
    }

    @Override
    public void onTrade(Trade trade) {
    	if (quoteListener != null)
    		quoteListener.onTrade(trade);
        if(quoteLogIsOpen)
        	log.debug("Trade Receive:S="+trade.getSymbol()+",I="+trade.getId()+",BS="+trade.getBuySellFlag()+",P="+trade.getPrice()+",V=" + trade.getQuantity());
        TradeEvent event = new TradeEvent(trade.getSymbol(), null, trade);
        eventProcessor.onEvent(event);
    }

    @Override
    public void onState(boolean on, IMarketDataAdaptor adaptor) {
        if (on) {
            log.info("MarketData feed is up-" + adaptor.getClass().getSimpleName());
            setState(true);
            preSubscribe(adaptor);
            eventManager.sendEvent(new MarketDataReadyEvent(null, true));
        } else {
            for (IMarketDataAdaptor adaptor1 : adaptors) {
                if (adaptor1.getState()) {
                    return;
                }
            }
            log.warn("MarketData feed is down");
            setState(false);
            eventManager.sendEvent(new MarketDataReadyEvent(null, false));
        }
    }

    protected void requestRequireData() throws Exception {
        log.debug("requestRequireData begin - " + serverInfo);
        IndexSessionRequestEvent isrEvent = new IndexSessionRequestEvent(requestDataEventkey, null, null);
        RefDataRequestEvent rdrEvent = new RefDataRequestEvent(requestDataEventkey, null);
        isrEvent.setReceiver(serverInfo);
        rdrEvent.setReceiver(serverInfo);
        eventManager.sendRemoteEvent(isrEvent);
        eventManager.sendRemoteEvent(rdrEvent);
        log.debug("requestRequireData end");
    }

    private void preSubscribe(IMarketDataAdaptor adaptor) {
        if (null == preSubscriptionList || preSubscriptionList.size() <= 0)
            return;

        isPreSubscribing = true;
        log.debug("Market data presubscribe: " + preSubscriptionList);
        try {
                log.debug("Market data presubscribe adapter begin : Adapter=" + adaptor.getClass().getSimpleName() + ",State="
                        + adaptor.getState());
                if (!adaptor.getState())
                    return;

                adaptor.subscribeMultiMarketData(preSubscriptionList, this);
        } catch (MarketDataException e) {
            log.error(e.getMessage(), e);
        } finally {
            isPreSubscribing = false;
        }
        log.debug("Market data presubscribe end");
    }

    private void checkEventAndSend(AsyncEvent event){
        if(eventListeners != null && eventListeners.size() > 0){
            for(IAsyncEventListener eventListener: eventListeners){
                if(eventListener != null) eventListener.onEvent(event);
            }
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

    public void setNonWait(boolean nonWait) {
        this.nonWait = nonWait;
    }

    public void setEventManager(IRemoteEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

	public IQuoteListener getQuoteListener() {
		return quoteListener;
	}

	public void setQuoteListener(IQuoteListener quoteListener) {
		this.quoteListener = quoteListener;
	}

    public void setEventListeners(List<IAsyncEventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }
}
