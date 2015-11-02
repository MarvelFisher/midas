package com.cyanspring.marketdata.item;

import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteSource;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.marketdata.adaptor.WindAdaptor;
import com.cyanspring.marketdata.data.IndexData;
import com.cyanspring.marketdata.type.WindDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IndexItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IndexItem.class);

    public static ConcurrentHashMap<String, IndexItem> indexItemBySymbolMap = new ConcurrentHashMap<String, IndexItem>();
    private String symbol;
    private long totalVolume = 0;
    private long volume = 0;

    public static IndexItem getItem(String symbol, boolean enableCreateNew) {

        synchronized (indexItemBySymbolMap) {
            if (indexItemBySymbolMap.containsKey(symbol)) {
                return indexItemBySymbolMap.get(symbol);
            }
            if (enableCreateNew) {
                IndexItem item = new IndexItem(symbol);
                indexItemBySymbolMap.put(symbol, item);
                return item;
            }
            return null;
        }
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

    public static void processIndexData(IndexData indexData, WindAdaptor windAdaptor) {

        String symbol = indexData.getWindCode();
        IndexItem item = getItem(symbol, true);

        // tick time
        String timeStamp = String.format("%d-%d", indexData.getTradingDay(),
                indexData.getTime());
        Date tickTime;

        try {
            if (indexData.getTime() < WindDef.AM10) {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = new Date();
        }

        List<QtyPrice> bids = new ArrayList<QtyPrice>();
        List<QtyPrice> asks = new ArrayList<QtyPrice>();
        Quote quote = new Quote(symbol, bids, asks);
        quote.setTimeStamp(tickTime);

        // bid/ask
        QtyPrice bid = bids.size() > 0 ? bids.get(0) : null;
        QtyPrice ask = asks.size() > 0 ? asks.get(0) : null;

        setBidAsk(quote, bid, ask);

        // update price
        quote.setOpen((double) indexData.getOpenIndex() / 10000);
        quote.setHigh((double) indexData.getHighIndex() / 10000);
        quote.setLow((double) indexData.getLowIndex() / 10000);
        quote.setLast((double) indexData.getLastIndex() / 10000);
        quote.setClose((double) indexData.getPrevIndex() / 10000);
        quote.setTurnover((double) indexData.getTurnover() * 100);

        // volume
        long totalVolume = indexData.getTotalVolume() * 100;

        if (PriceUtils.GreaterThan(totalVolume, item.totalVolume)) {
            item.volume = totalVolume - item.totalVolume;
            item.totalVolume = totalVolume;
        } else {
            item.volume = 0;
        }
        quote.setTotalVolume(totalVolume);
        quote.setLastVol(item.volume);

        //process send quote
        windAdaptor.sendInnerQuote(new InnerQuote(QuoteSource.WIND_INDEX, quote));
    }

    @Override
    public void close() throws Exception {
    }

    public IndexItem(String symbol) {
        this.symbol = symbol;
    }
}
