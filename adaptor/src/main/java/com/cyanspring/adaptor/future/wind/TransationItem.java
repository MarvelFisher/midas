package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.TransationData;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
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
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TransationItem implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IndexItem.class);

    protected static ConcurrentHashMap<String, TransationItem> transationItemBySymbolMap = new ConcurrentHashMap<>();
    private String symbolId;

    public static TransationItem getItem(String symbolId, boolean enableCreateNew) {

        synchronized (transationItemBySymbolMap) {
            if (transationItemBySymbolMap.containsKey(symbolId)) {
                return transationItemBySymbolMap.get(symbolId);
            }
            if (enableCreateNew) {
                TransationItem item = new TransationItem(symbolId);
                transationItemBySymbolMap.put(symbolId, item);
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

    public static void processTransationData(TransationData transationData, WindGateWayAdapter windGateWayAdapter, QuoteMgr quoteMgr) {

        String symbolId = transationData.getWindCode();
        TransationItem item = getItem(symbolId, true);

        //Get MarketSession
        String index = windGateWayAdapter.getMarketRuleBySymbolMap().get(symbolId);
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
        String timeStamp = String.format("%d-%d", transationData.getActionDay(),
                transationData.getTime());
        Date tickTime;

        try {
            if (transationData.getTime() < WindDef.AM10) {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = DateUtil.now();
        }

        //Check Quote
        if (PriceUtils.EqualLessThan(transationData.getTurnover(), 0)) {
            log.debug(WindDef.TITLE_TRANSATION + " " + WindDef.WARN_TURNOVER_LESS_THAN_ZERO + "," + transationData.getWindCode());
            return;
        }

        if (PriceUtils.EqualLessThan(transationData.getMatch(), 0)) {
            log.debug(WindDef.TITLE_TRANSATION + " " + WindDef.WARN_LAST_LESS_THAN_ZERO + "," + transationData.getWindCode());
            return;
        }

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

        Trade trade = new Trade();
        trade.setSymbol(symbolId);
        trade.setPrice((double) transationData.getMatch()/10000);
        trade.setQuantity(transationData.getVolume());
        trade.setBuySellFlag(transationData.getBuySellFlag());
        trade.setId(String.valueOf(transationData.getIndexNumber()));
        windGateWayAdapter.sendTrade(trade);
    }

    @Override
    public void close() throws Exception {
        FinalizeHelper.suppressFinalize(this);
    }

    public TransationItem(String symbolId) {
        this.symbolId = symbolId;
    }
}
