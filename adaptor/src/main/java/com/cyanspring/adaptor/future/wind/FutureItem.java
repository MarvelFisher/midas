package com.cyanspring.adaptor.future.wind;

import com.cyanspring.adaptor.future.wind.data.AbstractWindDataParser;
import com.cyanspring.adaptor.future.wind.data.FutureData;
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

public class FutureItem implements AutoCloseable {

    private static final Logger log = LoggerFactory
            .getLogger(FutureItem.class);

    protected static ConcurrentHashMap<String, FutureItem> futureItemBySymbolMap = new ConcurrentHashMap<String, FutureItem>();
    private String symbolId;
    private boolean isLogNoRefDataMessage = false;
    private int sessionStatus = -1;
    private long totalVolume = 0;
    private long volume = 0;
    private double settlePrice = 0;
    private long openInterest = 0;
    private double highLimit = 0;
    private double lowLimit = 0;
    private double preClose = 0;

    public static FutureItem getItem(String symbolId, boolean enableCreateNew) {

        synchronized (futureItemBySymbolMap) {
            if (futureItemBySymbolMap.containsKey(symbolId)) {
                return futureItemBySymbolMap.get(symbolId);
            }
            if (enableCreateNew) {
                FutureItem item = new FutureItem(symbolId);
                futureItemBySymbolMap.put(symbolId, item);
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

    public static void processFutureData(FutureData futureData) {

        String symbolId = futureData.getWindCode();
        FutureItem item = getItem(symbolId, true);

        //Get MarketSession
        String index = WindGateWayAdapter.marketRuleBySymbolMap.get(symbolId);
        if(index == null) {
            if(!item.isLogNoRefDataMessage) {
                log.debug(WindDef.TITLE_FUTURE + " " + WindDef.ERROR_NO_REFDATA + "," + futureData.getWindCode());
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
        String timeStamp = String.format("%d-%d", futureData.getTradingDay(),
                futureData.getTime());
        Date tickTime;

        try {
            if (futureData.getTime() < WindDef.AM10) {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = DateUtil.now();
        }

        if (PriceUtils.GreaterThan(futureData.getMatch(), 0)) {

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

            makeBidAskList(futureData.getBidPrice(), futureData.getBidVol(),
                    futureData.getAskPrice(), futureData.getAskVol(), bids, asks);

            Quote quote = new Quote(symbolId, bids, asks);

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
            quote.setClose((double) futureData.getPreClose() / 10000);
            quote.setTurnover((double) futureData.getTurnover());

            //Check Stale
            if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
                    || marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
                quote.setStale(true);
            }

            if (marketSessionData.getSessionType() == MarketSessionType.OPEN) {
                quote.setStale(false);
            }

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
            WindGateWayAdapter.instance.saveLastQuote(quote);
            WindGateWayAdapter.instance.sendInnerQuote(new InnerQuote(101, quote));
        }else{
            log.debug(WindDef.TITLE_FUTURE + " " + WindDef.WARN_LAST_LESS_THAN_ZERO + "," + futureData.getWindCode());
        }

        boolean quoteExtendIsChange = false;
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
        }

        double lowLimit = (double) futureData.getLowLimited() / 10000;
        if (PriceUtils.Compare(item.lowLimit, lowLimit) != 0) {
            item.lowLimit = lowLimit;
            quoteExtend.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExtendIsChange = true;
        }

        double preClose = (double) futureData.getPreClose() / 10000;
        if (PriceUtils.Compare(item.preClose, preClose) != 0) {
            item.preClose = preClose;
            quoteExtend.put(QuoteExtDataField.PRECLOSE.value(), preClose);
            quoteExtendIsChange = true;
        }

        int sessionStatus = AbstractWindDataParser.getItemSessionStatus(marketSessionData);
        if(sessionStatus!=item.sessionStatus){
            item.sessionStatus = sessionStatus;
            quoteExtend.put(QuoteExtDataField.SESSIONSTATUS.value(), sessionStatus);
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

    public FutureItem(String symbolId) {
        this.symbolId = symbolId;
    }

    @Override
    public void close() throws Exception {
        FinalizeHelper.suppressFinalize(this);
    }

    public static void main(String[] args) {
        String timeStamp = String.format("%d-%d", 20150318, 113000);
        Date tickTime;
        try {
            tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmss");
            tickTime = DateUtil.subDate(tickTime, 1, TimeUnit.SECONDS);
            System.out.println(tickTime);
            System.out.println(DateUtil.formatDate(tickTime, "yyyy-MM-dd"));
            System.out.println(TimeUtil.getTimePass(DateUtil.parseDate("20150318-120000", "yyyyMMdd-HHmmss"), tickTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
