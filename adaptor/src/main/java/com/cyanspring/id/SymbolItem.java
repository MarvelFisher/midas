package com.cyanspring.id;

import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.id.Library.Util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolItem implements AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(IdMarketDataAdaptor.class);

    @Autowired
    public static IdMarketDataAdaptor instance;
    String symbol;
    int dp;

    ArrayList<String> vecLast = new ArrayList<String>();
    long priceLastKey;
    double priceLast;

    long priceKey;
    double open;
    double high;
    double low;
    double price;
    double preclose;
    double close;
    ArrayList<Long> vecTickPrice = new ArrayList<Long>();

    TickItem tick = new TickItem();

    int status;

    /**
     * @return
     */
    public boolean isWatchID() {
        return symbol.equals("USDJPY");
    }

    /**
     * @return
     */
    public String getId() {
        return symbol;
    }

    /**
     * @param nDP
     * @return
     */
    static long getBase(int nDP) {
        final long arrBase[] = {1, 10, 100, 1000, 10000, 100000, 1000000};
        if (nDP >= 0 && nDP <= 6)
            return arrBase[nDP];

        long lBase = arrBase[6];
        for (int i = 6; i < nDP; i++)
            lBase *= 10;
        return lBase;
    }

    /**
     * @param dPrice
     * @param nDP
     * @return
     */
    long getPriceKey(Double dPrice, int nDP) {
        long lBase = getBase(nDP);
        long lPrice = (long) (dPrice * lBase + 0.5);
        dPrice = (double) lPrice / lBase;
        return lPrice;
    }

    /**
     * SymbolItem
     *
     * @param strID symbol ID e.g. USDJPY
     */
    SymbolItem(String strID) {
        clear(false);
        symbol = strID;
    }

    SymbolItem() {
        clear(false);
        symbol = "";
    }

    void uninit() {
        clear(false);
        vecTickPrice.clear();
    }

    @Override
    public void close() {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }

    /**
     * @param bSunrise
     */
    void clear(boolean bSunrise) {
        if (bSunrise) {
            if (0 == close) {
                return;
            }
            preclose = close;
            close = 0;
            if (bSunrise) {
                LogUtil.logInfo(log, "%s Sunrise Preclose=%s", symbol,
                        StringUtil.formatDouble(dp, preclose));
            }
        } else {
            close = preclose = 0.0;
        }
        status = 0;
        priceKey = 0;
        open = high = low = price = priceLast = 0.0;
        tick.clear();
    }

    /**
     *
     */
    public void doRefreshJob() {
        String strDoubleformat = String.format("%%.%df", dp);
        LogUtil.logInfo(log, "Refresh [%s ] High=%s Low= %s Preclose=%s",
                symbol, StringUtil.formatDouble(dp, high),
                String.format(strDoubleformat, low),
                String.format(strDoubleformat, preclose));

        ArrayList<String> vecTokens = new ArrayList<String>();

        // Preclose
        vecTokens.add(String.valueOf(preclose));

        // High
        vecTokens.add(String.valueOf(high));

        // Low
        vecTokens.add(String.valueOf(low));

    }

    /**
     * @param dPrice
     * @param dBidAskPrice
     * @return
     */
    public static boolean checkPrice(double dPrice, double dBidAskPrice) {

        double dLimit = 10;
        double dDiff = Math.abs(dBidAskPrice - dPrice);
        double dPercent = dDiff * 100 / dPrice;

        return dPercent < dLimit;
    }

    /**
     * @param timeRev
     * @param timeTick
     * @param nDP
     * @param dataByFieldIdMap
     */
    public void parseTick(Date timeRev, Date timeTick, int nDP,
                          ConcurrentHashMap<Integer, String> dataByFieldIdMap) {

        dp = nDP;
        boolean bTick = false;
        String contributeCode = null;

        if (dataByFieldIdMap.containsKey(FieldID.AskPrice)
                && dataByFieldIdMap.containsKey(FieldID.BidPrice)) {
            bTick = true;
            tick.setValue(FieldID.QuoteTime, timeTick);
        }

        Set<Integer> set = dataByFieldIdMap.keySet();
        Iterator<Integer> itr = set.iterator();
        while (itr.hasNext()) {
            int nField = itr.next();
            String strValue = dataByFieldIdMap.get(nField);
            if (strValue.isEmpty())
                continue;

            switch (nField) {
                case FieldID.AskPrice:
                case FieldID.BidPrice: {
                    double dValue = Double.parseDouble(strValue);
                    tick.setValue(nField, dValue);
                }
                break;
                case FieldID.Contributecode: {
                    contributeCode = strValue;
                }
                break;
                default:
                    break;
            }
        }

        double dPrice = 0;
        // long lPrice = 0;
        if (bTick) {
            dPrice = (tick.ask + tick.bid) / 2;
            dPrice = Double.parseDouble(FixStringBuilder.getString(dPrice, dp));
            // lPrice = getPriceKey(dPrice, dp);
            if (0.0 == dPrice
                    || false == checkPrice(dPrice, tick.ask)
                    || PriceUtils.EqualGreaterThan(tick.bid,tick.ask))
            {
                bTick = false;
            } else {
                tick.setValue(FieldID.CurrentPrice, dPrice);
            }
        }

        if (!bTick) {
            return;
        }

        tick.price = dPrice;
        price = dPrice;

        if (0.0 == preclose) {
            preclose = price;
        }

        if (0.0 == open) {
            open = dPrice;
        }

        if (0.0 == low || low > dPrice) {
            low = dPrice;
        }

        if (0.0 == high || high < dPrice) {
            high = dPrice;
        }

        Quote quote = getQuote();
//        quote.setTimeStamp(new Date());
        InnerQuote innerQuote = new InnerQuote(QuoteSource.ID, quote); //Id Adapter soureid = 2
        innerQuote.setContributor(contributeCode);
        innerQuote.setThrowQuoteTimeInterval(IdMarketDataAdaptor.instance.getThrowQuoteTimeInterval());
        IdMarketDataAdaptor.instance.sendInnerQuote(innerQuote);
    }

    /**
     * @return Quote
     */
    Quote getQuote() {
        Quote quote = new Quote(symbol, new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
        quote.setTimeStamp(tick.time);
        quote.setBid(tick.bid);
        quote.setAsk(tick.ask);
        if (price != 0)
            quote.setLast(price);
        if (open != 0)
            quote.setOpen(open);
        if (high != 0)
            quote.setHigh(high);
        if (low != 0)
            quote.setLow(low);
        if (preclose != 0)
            quote.setClose(preclose);
        return quote;
    }

    /**
     *
     */
    public void setClose() {
        close = price;
    }

    public void updateQuote(Quote quote) {

        Date timeQuoteDate = quote.getTimeStamp();
        timeQuoteDate = DateUtil.toLocal(timeQuoteDate);

        if (tick.time.getTime() > timeQuoteDate.getTime())
            return;

        open = quote.getOpen();
        high = quote.getHigh();
        low = quote.getLow();
        preclose = quote.getClose();
        price = quote.getLast();
        close = 0;

        tick.time = timeQuoteDate;
        tick.bid = quote.getBid();
        tick.ask = quote.getAsk();
        if (price == 0 && tick.bid > 0 && tick.ask > 0) {
            price = (tick.bid + tick.ask) / 2;
            price = Double.parseDouble(FixStringBuilder.getString(price, dp));
        }
        tick.price = price;
    }

    public String toString() {

        String strValue;

        FixStringBuilder header = new FixStringBuilder('=', '|');
        header.append("Open");
        header.append(open, dp);
        header.append("High");
        header.append(high, dp);
        header.append("Low");
        header.append(low, dp);
        header.append("Preclose");
        header.append(preclose, dp);
        header.append("Price");
        header.append(price, dp);
        header.append("Close");
        header.append(close, dp);
        char chSep = 0x06;
        strValue = String.format("%s%c%d%c%s%c%s%c%c", symbol, chSep, dp,
                chSep, header.toString(), chSep, tick.toString(dp), 0x0d, 0x0a);
        return strValue;
    }
}
