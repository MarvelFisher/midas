package com.cyanspring.marketdata.item;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.marketdata.adaptor.WindAdaptor;
import com.cyanspring.marketdata.data.TransationData;
import com.cyanspring.marketdata.type.WindDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class TransationItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IndexItem.class);

    public static ConcurrentHashMap<String, TransationItem> transationItemBySymbolMap = new ConcurrentHashMap<>();
    private String symbol;

    public static TransationItem getItem(String symbol, boolean enableCreateNew) {

        synchronized (transationItemBySymbolMap) {
            if (transationItemBySymbolMap.containsKey(symbol)) {
                return transationItemBySymbolMap.get(symbol);
            }
            if (enableCreateNew) {
                TransationItem item = new TransationItem(symbol);
                transationItemBySymbolMap.put(symbol, item);
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

    public static void processTransationData(TransationData transationData, WindAdaptor windAdaptor) {

        String symbolId = transationData.getWindCode();
        TransationItem item = getItem(symbolId, true);

        // tick time
        String timeStamp = String.format("%d-%d", transationData.getActionDay(),
                transationData.getTime());
        Date tickTime;

        try {
            if (transationData.getTime() < WindDef.AM10) {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = TimeUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = new Date();
        }

        Trade trade = new Trade();
        trade.setSymbol(symbolId);
        trade.setTimestamp(tickTime);
        trade.setPrice((double) transationData.getMatch()/10000);
        trade.setQuantity(transationData.getVolume());
        trade.setBuySellFlag(transationData.getBuySellFlag());
        trade.setId(String.valueOf(transationData.getIndexNumber()));
        windAdaptor.sendTrade(trade);
    }

    @Override
    public void close() throws Exception {
    }

    public TransationItem(String symbolId) {
        this.symbol = symbolId;
    }
}
