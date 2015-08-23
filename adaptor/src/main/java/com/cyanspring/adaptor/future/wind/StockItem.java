package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.AbstractWindDataParser;
import com.cyanspring.adaptor.future.wind.data.DataTimeStat;
import com.cyanspring.adaptor.future.wind.data.StockData;
import com.cyanspring.adaptor.future.wind.data.WindBaseDBData;
import com.cyanspring.adaptor.future.wind.refdata.WindRefDataAdapter;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.QuoteSource;
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
    private boolean isLogNoRefDataMessage = false;
    private int status = -1;
    private int sessionStatus = -1;
    private long totalVolume = 0;
    private long volume = 0;
    private double highLimit = 0;
    private double lowLimit = 0;
    private double preClose = 0;
    private long buyVol = 0;
    private long sellVol = 0;
    private long unclassifiedVol = 0;
    private long buyTurnover = 0;
    private long sellTurnover = 0;
    private long unclassifiedTurnover = 0;
    private long freeshares = 0;
    private long totalshares = 0;
    private double peRatio = 0;

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

    public static void processMarketData(StockData stockData, WindGateWayAdapter windGateWayAdapter, QuoteMgr quoteMgr) {

        String symbolId = stockData.getWindCode();
        StockItem item = getItem(symbolId, true);
        WindBaseDBData windBaseDBData = WindRefDataAdapter.windBaseDBDataHashMap.get(symbolId);

        //Get MarketSession
        String index = windGateWayAdapter.getMarketRuleBySymbolMap().get(symbolId);
        if (index == null) {
            if (!item.isLogNoRefDataMessage) {
                log.debug(WindDef.TITLE_STOCK + " " + WindDef.ERROR_NO_REFDATA + "," + stockData.getWindCode());
                item.isLogNoRefDataMessage = true;
            }
            return;
        }
        MarketSessionData marketSessionData = null;
        Date endDate;
        Date startDate;
        try {
            marketSessionData = windGateWayAdapter.getMarketSessionByIndexMap().get(index);
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

        if (stockData.getPreClose() > 0
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL_2
                ) {

            //modify tick Time
            if (quoteMgr.isModifyTickTime()) {
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
                        if (windGateWayAdapter.recordReceiveQuoteInfoBySymbolMap.containsKey(symbolId)) {
                            DataTimeStat dataTimeStat = windGateWayAdapter.recordReceiveQuoteInfoBySymbolMap.get(symbolId);
                            dataTimeStat.processReceiveQuoteTime(tickTime);
                        } else {
                            DataTimeStat dataTimeStat = new DataTimeStat(symbolId);
                            dataTimeStat.processReceiveQuoteTime(tickTime);
                            windGateWayAdapter.recordReceiveQuoteInfoBySymbolMap.put(symbolId, dataTimeStat);
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
            windGateWayAdapter.saveLastQuote(quote);
            windGateWayAdapter.sendInnerQuote(new InnerQuote(QuoteSource.WIND_GENERAL, quote));
        } else {
            log.debug(WindDef.TITLE_STOCK + " " + WindDef.WARN_PRECLOSE_LESS_THAN_ZERO + "," + stockData.getWindCode());
        }

        boolean quoteExtendIsChange = false;
        boolean specialQuoteExtendIsChange = false;
        DataObject quoteExtend = new DataObject();

        double highLimit = (double) stockData.getHighLimited() / 10000;
        if (PriceUtils.Compare(item.highLimit, highLimit) != 0) {
            item.highLimit = highLimit;
            quoteExtend.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
        }

        double lowLimit = (double) stockData.getLowLimited() / 10000;
        if (PriceUtils.Compare(item.lowLimit, lowLimit) != 0) {
            item.lowLimit = lowLimit;
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
        }

        long buyVol = stockData.getBuyVol();
        if (buyVol != item.buyVol) {
            item.buyVol = buyVol;
            quoteExtend.put(QuoteExtDataField.BUYVOL.value(), buyVol);
            quoteExtendIsChange = true;
        }

        long sellVol = stockData.getSellVol();
        if (sellVol != item.sellVol) {
            item.sellVol = sellVol;
            quoteExtend.put(QuoteExtDataField.SELLVOL.value(), sellVol);
            quoteExtendIsChange = true;
        }

        long unclassifiedVol = stockData.getUnclassifiedVol();
        if (unclassifiedVol != item.unclassifiedVol) {
            item.unclassifiedVol = unclassifiedVol;
            quoteExtend.put(QuoteExtDataField.UNCLASSIFIEDVOL.value(), unclassifiedVol);
            quoteExtendIsChange = true;
        }

        long buyTurnover = stockData.getBuyTurnover();
        if (buyTurnover != item.buyTurnover) {
            item.buyTurnover = buyTurnover;
            quoteExtend.put(QuoteExtDataField.BUYTURNOVER.value(), buyTurnover);
            quoteExtendIsChange = true;
        }

        long sellTurnover = stockData.getSellTurnover();
        if (sellTurnover != item.sellTurnover) {
            item.sellTurnover = sellTurnover;
            quoteExtend.put(QuoteExtDataField.SELLTURNOVER.value(), sellTurnover);
            quoteExtendIsChange = true;
        }

        long unclassifiedTurnover = stockData.getUnclassifiedTurnover();
        if (unclassifiedTurnover != item.unclassifiedTurnover) {
            item.unclassifiedTurnover = unclassifiedTurnover;
            quoteExtend.put(QuoteExtDataField.UNCLASSIFIEDTURNOVER.value(), unclassifiedTurnover);
            quoteExtendIsChange = true;
        }

        double preClose = (double) stockData.getPreClose() / 10000;
        if (PriceUtils.Compare(item.preClose, preClose) != 0) {
            item.preClose = preClose;
            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
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

        //get from Wind Base DB
        if(windBaseDBData != null) {
            long freeshares = windBaseDBData.getFreeShares();
            if (freeshares != item.freeshares) {
                item.freeshares = freeshares;
                quoteExtend.put(QuoteExtDataField.FREESHARES.value(), freeshares);
                quoteExtendIsChange = true;
            }
            long totalshares = windBaseDBData.getTotalShares();
            if (totalshares != item.totalshares) {
                item.totalshares = totalshares;
                quoteExtend.put(QuoteExtDataField.TOTOALSHARES.value(), totalshares);
                quoteExtendIsChange = true;
            }
            double peRatio = windBaseDBData.getPERatio();
            if (peRatio != item.peRatio) {
                item.peRatio = peRatio;
                quoteExtend.put(QuoteExtDataField.PERATIO.value(), peRatio);
                quoteExtendIsChange = true;
            }
        }

        //Ceil,Floor,preClose must together send
        if (specialQuoteExtendIsChange) {
            quoteExtend.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
            if(PriceUtils.Equal(highLimit, 0) || PriceUtils.Equal(lowLimit, 0) || PriceUtils.Equal(preClose, 0)){
                quoteExtend.remove(QuoteExtDataField.CEIL.value());
                quoteExtend.remove(QuoteExtDataField.FLOOR.value());
                quoteExtend.remove(QuoteExtDataField.PRECLOSE.value());
            }
        }

        // process send quote Extend
        if (quoteExtendIsChange && quoteExtend.getFields().size() > 0) {
            quoteExtend.put(QuoteExtDataField.SYMBOL.value(), symbolId);
            quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(), tickTime);
            windGateWayAdapter.saveLastQuoteExtend(quoteExtend);
            windGateWayAdapter.sendQuoteExtend(quoteExtend);
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
