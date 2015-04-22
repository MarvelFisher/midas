package com.cyanspring.adaptor;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.marketdata.*;
import com.cyanspring.common.event.marketsession.TradeDateEvent;
import com.cyanspring.common.marketdata.*;
import com.cyanspring.id.Library.Util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
public class MarketDataManager extends MarketDataReceiver {
    private static final Logger log = LoggerFactory.getLogger(MarketDataManager.class);
    private IQuoteSaver quoteSaver;
    private String tickDir = "ticks";
    private String lastQuoteFile = "last.xml";
    private String lastQuoteExtendFile = "lastExtend.xml";
    private String lastTradeDateQuoteFile = "last_tdq.xml";
    private String lastTradeDateQuoteExtendFile = "lastExtend_tdq.xml";

    public MarketDataManager(List<IMarketDataAdaptor> adaptors) {
        super(adaptors);
    }

    public void processQuoteRequestEvent(QuoteRequestEvent event) {

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
        } else {
            if (preSubscriptionList != null) {

            }
        }
        super.init();
    }

    @Override
    protected List<Class> subscuribeEvent() {
        ArrayList<Class> clzList = new ArrayList<Class>();
        clzList.add(QuoteRequestEvent.class);
        clzList.add(TradeSubEvent.class);
        clzList.add(QuoteExtSubEvent.class);
        clzList.add(QuoteSubEvent.class);
        return clzList;
    }

    @Override
    protected void sendQuoteEvent(QuoteEvent event) {
        try {
            eventManager.sendGlobalEvent(event);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processQuoteSubEvent(QuoteSubEvent event) throws Exception {
        log.debug("QuoteSubEvent: " + event.getSymbol() + ", " + event.getReceiver());
        String symbol = event.getSymbol();
        Quote quote = quotes.get(symbol);

        if (quote != null) {
            eventManager.sendLocalOrRemoteEvent(new QuoteEvent(event.getKey(), event.getSender(), quote));
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

    public void processTradeSubEvent(TradeSubEvent event)
            throws MarketDataException {
        String symbol = event.getSymbol();
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

    @Override
    public void processAsyncTimerEvent(AsyncTimerEvent event){
        super.processAsyncTimerEvent(event);
        if (quoteSaver != null) {
            quoteSaver.saveLastQuoteToFile(tickDir + "/" + lastQuoteFile, quotes);
            quoteSaver.saveLastQuoteExtendToFile(tickDir + "/" + lastQuoteExtendFile, quoteExtends);
        }
    }

    @Override
    public boolean processTradeDateEvent(TradeDateEvent event){
        if (super.processTradeDateEvent(event)){
            if (quoteSaver != null) {
                quoteSaver.saveLastTradeDateQuoteToFile(tickDir + "/" + lastTradeDateQuoteFile, quotes, lastTradeDateQuotes);
                quoteSaver.saveLastTradeDateQuoteExtendToFile(tickDir + "/" + lastTradeDateQuoteExtendFile, quoteExtends, lastTradeDateQuoteExtends);
            }
        }
        return true;
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
            if ((quoteExtendSegmentMap != null && quoteExtendSegmentMap.size() > 0) || (quoteExtendSegmentMap == null && transQuoteExtendOffset == 0)) {
                totalQuoteExtendCount = totalQuoteExtendCount + (quoteExtendSegmentMap != null ? quoteExtendSegmentMap.size() : 0);
                MultiQuoteExtendEvent multiQuoteExtendEvent = new MultiQuoteExtendEvent(event.getKey(), event.getSender()
                        , quoteExtendSegmentMap, DateUtil.parseDate(tradeDate, "yyyy-MM-dd"));
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

    public void setQuoteSaver(IQuoteSaver quoteSaver) {
        this.quoteSaver = quoteSaver;
    }
}
