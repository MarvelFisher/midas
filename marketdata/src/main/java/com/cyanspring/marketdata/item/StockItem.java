package com.cyanspring.marketdata.item;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.marketdata.adaptor.WindAdaptor;
import com.cyanspring.marketdata.data.StockData;
import com.cyanspring.marketdata.type.WindDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StockItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StockItem.class);

    public static ConcurrentHashMap<String, StockItem> stockItemBySymbolMap = new ConcurrentHashMap<String, StockItem>();
    private String symbol;
    private int status = -1;
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

    public static void processMarketData(StockData stockData, WindAdaptor windAdaptor) {

        String symbol = stockData.getWindCode();
        StockItem item = getItem(symbol, true);
        boolean quoteExtendIsChange = false;
        boolean specialQuoteExtendIsChange = false;
        DataObject quoteExtend = new DataObject();

        // tick time
        String timeStamp = String.format("%d-%d", stockData.getTradingDay(),
                stockData.getTime());
        Date tickTime;

        try {
            if (stockData.getTime() < WindDef.AM10) {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = new Date();
        }

        if (stockData.getPreClose() > 0
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL
                || stockData.getStatus() == WindDef.STOCK_STATUS_STOP_SYMBOL_2
                ) {


            List<QtyPrice> bids = new ArrayList<QtyPrice>();
            List<QtyPrice> asks = new ArrayList<QtyPrice>();

            makeBidAskList(stockData.getBidPrice(), stockData.getBidVol(),
                    stockData.getAskPrice(), stockData.getAskVol(), bids, asks);

            Quote quote = new Quote(symbol, bids, asks);

            quote.setTimeStamp(tickTime);

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
            windAdaptor.sendInnerQuote(new InnerQuote(QuoteSource.WIND_GENERAL, quote));
        } else {
            log.debug(WindDef.TITLE_STOCK + " " + WindDef.WARN_PRECLOSE_LESS_THAN_ZERO + "," + stockData.getWindCode());
        }

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

        int status = stockData.getStatus();
        if (status != item.status) {
            item.status = status;
            quoteExtend.put(QuoteExtDataField.STATUS.value(), status);
            quoteExtendIsChange = true;
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
            quoteExtend.put(QuoteExtDataField.SYMBOL.value(), symbol);
            quoteExtend.put(QuoteExtDataField.TIMESTAMP.value(), tickTime);
            windAdaptor.sendQuoteExtend(quoteExtend);
        }
    }

    @Override
    public void close() throws Exception {
    }

    public StockItem(String symbolId) {
        this.symbol = symbolId;
    }
}
