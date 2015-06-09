package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.AbstractWindDataParser;
import com.cyanspring.adaptor.future.wind.data.DataTimeStat;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FinalizeHelper;
import com.cyanspring.id.Library.Util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StockItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StockItem.class);

    protected static ConcurrentHashMap<String, StockItem> stockItemBySymbolMap = new ConcurrentHashMap<String, StockItem>();
    private String symbolId;
    private int status = -1;
    private int sessionStatus = -1;
    private long totalVolume = 0;
    private long volume = 0;
    private double highLimit = 0;
    private double lowLimit = 0;

    public static StockItem getItem(String symbolId, boolean enableCreateNew) {

        synchronized (stockItemBySymbolMap) {
            if (stockItemBySymbolMap.containsKey(symbolId)) {
                return stockItemBySymbolMap.get(symbolId);
            }
            if (enableCreateNew) {
                StockItem item = new StockItem(symbolId);
                stockItemBySymbolMap.put(symbolId, item);
                return item;
            }
            return null;
        }
    }

    public static boolean makeBidAskList(long[] bids, long[] bidsizes,
                                         long[] asks, long[] asksizes, List<QtyPrice> bidList,
                                         List<QtyPrice> askList) {

        for (int i = 0; i < bids.length; i++) {
            double price = (double) bids[i] / 10000;
            long size = bidsizes[i];
            QtyPrice bidask = new QtyPrice(size, price);
            bidList.add(bidask);
        }

        for (int i = 0; i < asks.length; i++) {
            double price = (double) asks[i] / 10000;
            long size = asksizes[i];
            QtyPrice bidask = new QtyPrice(size, price);
            askList.add(bidask);
        }

        return true;
    }

    public static void setBidAsk(Quote quote, QtyPrice bid, QtyPrice ask) {
        if (bid != null) {
            quote.setBid(bid.getPrice());
            quote.setBidVol(bid.getQuantity());
        }
        if (ask != null) {
            quote.setAsk(ask.getPrice());
            quote.setAskVol(ask.getQuantity());
        }
    }

    public static void processMarketData(StockData stockData) {

        String symbolId = stockData.getWindCode();
        StockItem item = getItem(symbolId, true);

        //Get MarketSession
        String index = WindGateWayAdapter.marketRuleBySymbolMap.get(symbolId);
        MarketSessionData marketSessionData = null;
        Date endDate;
        Date startDate;
        try {
            marketSessionData = WindGateWayAdapter.marketSessionByIndexMap.get(index);
            endDate = marketSessionData.getEndDate();
            startDate = marketSessionData.getStartDate();
        } catch (Exception e) {
            LogUtil.logException(log, e);
            return;
        }

        // tick time
        String timeStamp = String.format("%d-%d", stockData.getTradingDay(),
                stockData.getTime());
        Date tickTime;

        try {
            if (stockData.getTime() < WindDef.AM10) {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = DateUtil.now();
        }

        if (PriceUtils.GreaterThan(stockData.getMatch(), 0)
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL_2
                ) {

            //modify tick Time
            if (QuoteMgr.isModifyTickTime()) {
                if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
                        && DateUtil.compareDate(tickTime, endDate) < 0) {
                    tickTime = endDate;
                }

                if (marketSessionData.getSessionType() == MarketSessionType.OPEN
                        && DateUtil.compareDate(tickTime, endDate) >= 0) {
                    tickTime = DateUtil.subDate(endDate, 1, TimeUnit.SECONDS);
                }

                if (marketSessionData.getSessionType() == MarketSessionType.CLOSE
                        && DateUtil.compareDate(tickTime, startDate) >= 0) {
                    if (TimeUtil.getTimePass(tickTime, startDate) <= WindDef.SmallSessionTimeInterval)
                        tickTime = DateUtil.subDate(startDate, 1, TimeUnit.SECONDS);
                    if (TimeUtil.getTimePass(endDate, tickTime) <= WindDef.SmallSessionTimeInterval)
                        tickTime = endDate;
                }
            }

            List<QtyPrice> bids = new ArrayList<QtyPrice>();
            List<QtyPrice> asks = new ArrayList<QtyPrice>();

            makeBidAskList(stockData.getBidPrice(), stockData.getBidVol(),
                    stockData.getAskPrice(), stockData.getAskVol(), bids, asks);

            Quote quote = new Quote(symbolId, bids, asks);

            quote.setTimeStamp(tickTime);

            //Check Stale
            if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
                    || marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
                quote.setStale(true);
            }
            if (marketSessionData.getSessionType() == MarketSessionType.OPEN) {
                switch (stockData.getStatus()) {
                    case WindDef.STOCK_STATUS_MARKET_CLOSE:
                    case WindDef.STOCK_STATUS_STOP_TRAN:
                    case WindDef.STOCK_STATUS_NEW_SYMBOL:
                    case WindDef.STOCK_STATUS_NOT_SERVICE:
                    case WindDef.STOCK_STATUS_PENDING:
                    case WindDef.STOCK_STATUS_PENDING_2:
                    case WindDef.STOCK_STATUS_SLEEP:
                    case WindDef.STOCK_STATUS_STOP_SYMBOL:
                    case WindDef.STOCK_STATUS_STOP_SYMBOL_2:
                    case WindDef.STOCK_STATUS_STOP_TRA_IN_OPEN:
                    case WindDef.STOCK_STATUS_WAIT_DELETE:
                        quote.setStale(true);
                        break;
                    default:
                        quote.setStale(false);
                        //record time stat
                        if(WindGateWayAdapter.instance.recordReceiveQuoteInfoBySymbolMap.containsKey(symbolId)){
                            DataTimeStat dataTimeStat = WindGateWayAdapter.instance.recordReceiveQuoteInfoBySymbolMap.get(symbolId);
                            dataTimeStat.processReceiveQuoteTime(tickTime);
                        }else{
                            DataTimeStat dataTimeStat = new DataTimeStat(symbolId);
                            dataTimeStat.processReceiveQuoteTime(tickTime);
                            WindGateWayAdapter.instance.recordReceiveQuoteInfoBySymbolMap.put(symbolId, dataTimeStat);
                        }
                        break;
                }
            }

            // bid/ask
            QtyPrice bid = bids.size() > 0 ? bids.get(0) : null;
            QtyPrice ask = asks.size() > 0 ? asks.get(0) : null;

            setBidAsk(quote, bid, ask);

            // update price
            quote.setOpen((double) stockData.getOpen() / 10000);
            quote.setHigh((double) stockData.getHigh() / 10000);
            quote.setLow((double) stockData.getLow() / 10000);
            quote.setLast((double) stockData.getMatch() / 10000);
            quote.setClose((double) stockData.getPreClose() / 10000);
            quote.setTurnover((double) stockData.getTurnover());

            // volume
            long totalVolume = stockData.getVolume();

            if (PriceUtils.GreaterThan(totalVolume, item.totalVolume)) {
                item.volume = totalVolume - item.totalVolume;
                item.totalVolume = totalVolume;
            } else {
                item.volume = 0;
            }
            quote.setTotalVolume(totalVolume);
            quote.setLastVol(item.volume);

            //process send quote
            WindGateWayAdapter.instance.saveLastQuote(quote);
            WindGateWayAdapter.instance.sendInnerQuote(new InnerQuote(101, quote));
        } else {
            log.debug(WindDef.TITLE_STOCK + " " + WindDef.WARN_LAST_LESS_THAN_ZERO + "," + stockData.getWindCode());
        }

        boolean quoteExtendIsChange = false;
        DataObject quoteExtend = new DataObject();

        double highLimit = (double) stockData.getHighLimited() / 10000;
        if (PriceUtils.Compare(item.highLimit, highLimit) != 0) {
            item.highLimit = highLimit;
            quoteExtend.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExtendIsChange = true;
        }

        double lowLimit = (double) stockData.getLowLimited() / 10000;
        if (PriceUtils.Compare(item.lowLimit, lowLimit) != 0) {
            item.lowLimit = lowLimit;
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtendIsChange = true;
        }

        int sessionStatus = AbstractWindDataParser.getItemSessionStatus(marketSessionData);
        if (sessionStatus != item.sessionStatus) {
            item.sessionStatus = sessionStatus;
            quoteExtend.put(QuoteExtDataField.SESSIONSTATUS.value(), sessionStatus);
            quoteExtendIsChange = true;
        }

        int status = stockData.getStatus();
        if (status != item.status) {
            item.status = status;
            quoteExtend.put(QuoteExtDataField.STATUS.value(), status);
            quoteExtendIsChange = true;
        }

        // process send quote Extend
        if (quoteExtendIsChange) {
            quoteExtend.put(QuoteExtDataField.SYMBOL.value(), symbolId);
            quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(), tickTime);
            WindGateWayAdapter.instance.saveLastQuoteExtend(quoteExtend);
            WindGateWayAdapter.instance.sendQuoteExtend(quoteExtend);
        }
    }

    @Override
    public void close() throws Exception {
        FinalizeHelper.suppressFinalize(this);
    }

    public StockItem(String symbolId) {
        this.symbolId = symbolId;
    }
}
