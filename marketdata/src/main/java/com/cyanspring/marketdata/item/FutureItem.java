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
import com.cyanspring.marketdata.data.FutureData;
import com.cyanspring.marketdata.type.WindDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FutureItem implements AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(FutureItem.class);

    public static ConcurrentHashMap<String, FutureItem> futureItemBySymbolMap = new ConcurrentHashMap<String, FutureItem>();
    private String symbol;
    private long totalVolume = 0;
    private long volume = 0;
    private double settlePrice = 0;
    private long openInterest = 0;
    private double highLimit = 0;
    private double lowLimit = 0;
    private double preClose = 0;

    public static FutureItem getItem(String symbol, boolean enableCreateNew) {

        synchronized (futureItemBySymbolMap) {
            if (futureItemBySymbolMap.containsKey(symbol)) {
                return futureItemBySymbolMap.get(symbol);
            }
            if (enableCreateNew) {
                FutureItem item = new FutureItem(symbol);
                futureItemBySymbolMap.put(symbol, item);
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

    public static void processFutureData(FutureData futureData, WindAdaptor windAdaptor) {

        String symbol = futureData.getWindCode();
        FutureItem item = getItem(symbol, true);

        // tick time
        String timeStamp = String.format("%d-%d", futureData.getTradingDay(),
                futureData.getTime());
        Date tickTime;

        try {
            if (futureData.getTime() < WindDef.AM10) {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = new Date();
        }

        if (futureData.getPreSettlePrice() > 0) {

            List<QtyPrice> bids = new ArrayList<QtyPrice>();
            List<QtyPrice> asks = new ArrayList<QtyPrice>();

            makeBidAskList(futureData.getBidPrice(), futureData.getBidVol(),
                    futureData.getAskPrice(), futureData.getAskVol(), bids, asks);

            Quote quote = new Quote(symbol, bids, asks);

            quote.setTimeStamp(tickTime);

            // bid/ask
            QtyPrice bid = bids.size() > 0 ? bids.get(0) : null;
            QtyPrice ask = asks.size() > 0 ? asks.get(0) : null;

            setBidAsk(quote, bid, ask);

            // update price
            quote.setOpen((double) futureData.getOpen() / 10000);
            quote.setHigh((double) futureData.getHigh() / 10000);
            quote.setLow((double) futureData.getLow() / 10000);
            quote.setLast((double) futureData.getMatch() / 10000);
            quote.setClose((double) futureData.getPreSettlePrice() / 10000);
            quote.setTurnover((double) futureData.getTurnover());

            //volume
            long totalVolume = futureData.getVolume();

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
            log.debug(WindDef.TITLE_FUTURE + " " + WindDef.WARN_PRECLOSE_LESS_THAN_ZERO + "," + futureData.getWindCode());
        }

        boolean quoteExtendIsChange = false;
        boolean specialQuoteExtendIsChange = false;
        DataObject quoteExtend = new DataObject();

        double settlePrice = (double) futureData.getSettlePrice() / 10000;
        if (PriceUtils.Compare(item.settlePrice, settlePrice) != 0) {
            item.settlePrice = settlePrice;
            quoteExtend.put(QuoteExtDataField.SETTLEPRICE.value(), settlePrice);
            quoteExtendIsChange = true;
        }

        long openInterest = futureData.getOpenInterest();
        if (PriceUtils.Compare(item.openInterest, openInterest) != 0) {
            item.openInterest = openInterest;
            quoteExtend.put(QuoteExtDataField.OPENINTEREST.value(), openInterest);
            quoteExtendIsChange = true;
        }

        double highLimit = (double) futureData.getHighLimited() / 10000;
        if (PriceUtils.Compare(item.highLimit, highLimit) != 0) {
            item.highLimit = highLimit;
            quoteExtend.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
        }

        double lowLimit = (double) futureData.getLowLimited() / 10000;
        if (PriceUtils.Compare(item.lowLimit, lowLimit) != 0) {
            item.lowLimit = lowLimit;
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
        }

        double preClose = (double) futureData.getPreSettlePrice() / 10000;
        if (PriceUtils.Compare(item.preClose, preClose) != 0) {
            item.preClose = preClose;
            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
            quoteExtendIsChange = true;
            specialQuoteExtendIsChange = true;
        }

        //Ceil,Floor,preClose must together send
        if (specialQuoteExtendIsChange) {
            quoteExtend.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
            if (PriceUtils.Equal(highLimit, 0) || PriceUtils.Equal(lowLimit, 0) || PriceUtils.Equal(preClose, 0)) {
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

    public FutureItem(String symbolId) {
        this.symbol = symbolId;
    }

    @Override
    public void close() throws Exception {
    }
}


