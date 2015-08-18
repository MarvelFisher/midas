package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.IndexData;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
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

public class IndexItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IndexItem.class);

    protected static ConcurrentHashMap<String, IndexItem> indexItemBySymbolMap = new ConcurrentHashMap<String, IndexItem>();
    private boolean isLogNoRefDataMessage = false;
    private String symbolId;
    private long totalVolume = 0;
    private long volume = 0;

    public static IndexItem getItem(String symbolId, boolean enableCreateNew) {

        synchronized (indexItemBySymbolMap) {
            if (indexItemBySymbolMap.containsKey(symbolId)) {
                return indexItemBySymbolMap.get(symbolId);
            }
            if (enableCreateNew) {
                IndexItem item = new IndexItem(symbolId);
                indexItemBySymbolMap.put(symbolId, item);
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

    public static void processIndexData(IndexData indexData) {

        String symbolId = indexData.getWindCode();
        IndexItem item = getItem(symbolId, true);

        //Get MarketSession
        String index = WindGateWayAdapter.marketRuleBySymbolMap.get(symbolId);
        if(index == null) {
            if(!item.isLogNoRefDataMessage) {
                log.debug(WindDef.TITLE_INDEX + " " + WindDef.ERROR_NO_REFDATA + "," + indexData.getWindCode());
                item.isLogNoRefDataMessage = true;
            }
            return;
        }
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
        String timeStamp = String.format("%d-%d", indexData.getTradingDay(),
                indexData.getTime());
        Date tickTime;

        try {
            if (indexData.getTime() < WindDef.AM10) {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = DateUtil.now();
        }

        //Check Quote
        if (PriceUtils.EqualLessThan(indexData.getTurnover(), 0)) {
            log.debug(WindDef.TITLE_INDEX + " " + WindDef.WARN_TURNOVER_LESS_THAN_ZERO + "," + indexData.getWindCode());
            return;
        }

        if (PriceUtils.EqualLessThan(indexData.getLastIndex(), 0)) {
            log.debug(WindDef.TITLE_INDEX + " " + WindDef.WARN_LAST_LESS_THAN_ZERO + "," + indexData.getWindCode());
            return;
        }

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
        Quote quote = new Quote(symbolId, bids, asks);
        quote.setTimeStamp(tickTime);

        //Check Stale
        if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
                || marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
            quote.setStale(true);
        }

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
        WindGateWayAdapter.instance.saveLastQuote(quote);
        WindGateWayAdapter.instance.sendInnerQuote(new InnerQuote(102, quote));
    }

    @Override
    public void close() throws Exception {
        FinalizeHelper.suppressFinalize(this);
    }

    public IndexItem(String symbolId) {
        this.symbolId = symbolId;
    }
}
