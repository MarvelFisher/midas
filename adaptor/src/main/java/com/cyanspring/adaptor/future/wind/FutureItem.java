package com.cyanspring.adaptor.future.wind;

import cn.com.wind.td.tdf.TDF_CODE;
import cn.com.wind.td.tdf.TDF_FUTURE_DATA;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.SymbolInfo;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.id.Library.Util.*;
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
    private int tDate = 0;
    private long totalVolume = 0;
    private long volume = 0;
    private double settlePrice = 0;
    private long openInterest = 0;
    private double highLimit = 0;
    private double lowLimit = 0;
//    private static int lastShow = 0;
    private String market;
    private String cnName;
    private String enName;

    public static FutureItem getItem(String symbolId, String windCode,
                                     boolean enableCreateNew) {

        synchronized (futureItemBySymbolMap) {
            if (futureItemBySymbolMap.containsKey(symbolId) == true) {
                return futureItemBySymbolMap.get(symbolId);
            }

            // else
            if (enableCreateNew) {
                FutureItem item = new FutureItem(symbolId);
                if (WindFutureDataAdaptor.instance.isGateway())
                    item.setMarket(windCode.split("\\.")[1]);
                futureItemBySymbolMap.put(symbolId, item);
                return item;
            }
            return null;
        }
    }

    public static List<SymbolInfo> getSymbolInfoList() {
        List<FutureItem> list = new ArrayList<FutureItem>();
        synchronized (futureItemBySymbolMap) {
            list.addAll(futureItemBySymbolMap.values());
        }

        List<SymbolInfo> outList = new ArrayList<SymbolInfo>();
        for (FutureItem item : list) {
            SymbolInfo info = item.getSymbolInfo();
            outList.add(info);
        }
        return outList;
    }

    public static void clearSymbols() {
        List<FutureItem> list = new ArrayList<FutureItem>();
        synchronized (futureItemBySymbolMap) {
            list.addAll(futureItemBySymbolMap.values());
            futureItemBySymbolMap.clear();
        }
        for (FutureItem item : list) {
            try {
                item.close();
            } catch (Exception e) {
                LogUtil.logException(log, e);
            }
        }
        list.clear();
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

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public static SymbolInfo processCODE(TDF_CODE code) {
        String symbolId = code.getCode();
        String windCode = code.getWindCode();
        FutureItem item = FutureItem.getItem(symbolId, windCode, true);

        item.setMarket(code.getMarket());
        String cnName = WindFutureDataAdaptor.convertGBString(code.getCNName());
        item.setCnName(cnName);
        String enName = code.getENName();
        if (enName.isEmpty()) {
            enName = symbolId;
        }
        item.setEnName(enName);

        return item.getSymbolInfo();

    }

    public static void processFutureData(TDF_FUTURE_DATA data) {

        String symbolId = data.getWindCode();
        String windCode = data.getWindCode();

        FutureItem item = getItem(symbolId, windCode, true);

        data.getStatus();

        List<QtyPrice> bids = new ArrayList<QtyPrice>();
        List<QtyPrice> asks = new ArrayList<QtyPrice>();

        makeBidAskList(data.getBidPrice(), data.getBidVol(),
                data.getAskPrice(), data.getAskVol(), bids, asks);

        Quote quote = new Quote(symbolId, bids, asks);

        //Get MarketSession
        String index = WindFutureDataAdaptor.marketRuleBySymbolMap.get(symbolId);
        MarketSessionData marketSessionData = null;
        Date endDate;
        Date startDate;
        try {
            marketSessionData = WindFutureDataAdaptor.marketSessionByIndexMap.get(index);
            endDate = marketSessionData.getEndDate();
            startDate = marketSessionData.getStartDate();
        } catch (Exception e) {
            LogUtil.logException(log, e);
            return;
        }

        // tick time
        String timeStamp = String.format("%d-%d", data.getTradingDay(),
                data.getTime());
        Date tickTime;

        try {
            if (data.getTime() < WindFutureDataAdaptor.AM10) {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HmmssSSS");
            } else {
                tickTime = DateUtil.parseDate(timeStamp, "yyyyMMdd-HHmmssSSS");
            }
        } catch (ParseException e) {
            tickTime = DateUtil.now();
        }

        //modify tick Time
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
            if (TimeUtil.getTimePass(tickTime, startDate) <= WindFutureDataAdaptor.SmallSessionTimeInterval)
                tickTime = DateUtil.subDate(startDate, 1, TimeUnit.SECONDS);
            if (TimeUtil.getTimePass(endDate, tickTime) <= WindFutureDataAdaptor.SmallSessionTimeInterval)
                tickTime = endDate;
        }

        quote.setTimeStamp(tickTime);

        // bid/ask
        QtyPrice bid = bids.size() > 0 ? bids.get(0) : null;
        QtyPrice ask = asks.size() > 0 ? asks.get(0) : null;

        setBidAsk(quote, bid, ask);

        // update price
        quote.setOpen((double) data.getOpen() / 10000);
        quote.setHigh((double) data.getHigh() / 10000);
        quote.setLow((double) data.getLow() / 10000);
        quote.setLast((double) data.getMatch() / 10000);
        quote.setClose((double) data.getPreClose() / 10000);


        //Check Stale
        if (marketSessionData.getSessionType() == MarketSessionType.PREOPEN
                || marketSessionData.getSessionType() == MarketSessionType.CLOSE) {
            quote.setStale(true);
        }

        if (marketSessionData.getSessionType() == MarketSessionType.OPEN) {
            quote.setStale(false);
        }

        boolean change = false;

        double settlePrice = (double) data.getSettlePrice() / 10000;
        if (item.settlePrice != settlePrice) {
            item.settlePrice = settlePrice;
            change = true;
        }

        long openInterest = data.getOpenInterest();
        if (item.openInterest != openInterest) {
            item.openInterest = openInterest;
            change = true;
        }

        double highLimit = (double) data.getHighLimited() / 10000;
        if (item.highLimit != highLimit) {
            item.highLimit = highLimit;
            change = true;
        }

        double lowLimit = (double) data.getLowLimited() / 10000;
        if (item.lowLimit != lowLimit) {
            item.lowLimit = lowLimit;
            change = true;
        }

        //volume
        long totalVolume = data.getVolume();

        if (totalVolume - item.totalVolume > 0) {
            item.volume = totalVolume - item.totalVolume;
            item.totalVolume = totalVolume;
        } else {
            item.volume = 0;
        }
        quote.setTotalVolume(totalVolume);
        quote.setLastVol(item.volume);

        // process Extend field
        DataObject quoteExt = null;
        if (change) {
            quoteExt = new DataObject();
            quoteExt.put(QuoteExtDataField.SYMBOL.value(), symbolId);
            quoteExt.put(QuoteExtDataField.ID.value(), quote.getId());
            quoteExt.put(QuoteExtDataField.TIMESTAMP.value(), tickTime);
            quoteExt.put(QuoteExtDataField.CEIL.value(), highLimit);
            quoteExt.put(QuoteExtDataField.FLOOR.value(), lowLimit);
            quoteExt.put(QuoteExtDataField.SETTLEPRICE.value(), settlePrice);
            quoteExt.put(QuoteExtDataField.OPENINTEREST.value(), openInterest);
        }


        // fire quote event
        String s = quote.toString();
        WindFutureDataAdaptor.instance.saveLastQuote(quote, quoteExt);
        WindFutureDataAdaptor.instance.sendInnerQuote(new InnerQuote(101, quote), quoteExt);

//        Date now = DateUtil.now();
//        int timestamp = DateUtil.dateTime2Time(now);
//        // show quote
//        FutureFeed future = FutureFeed.instance;
//        if (future.isSelectAll || future.isWatchSymbol(symbolId)) {
//            if (timestamp != lastShow) {
//                FutureFeed.instance.showQuote(quote);
//            }
//        }
    }

    public String windCode() {
        return String.format(symbolId);
    }

    public SymbolInfo getSymbolInfo() {
        SymbolInfo info = new SymbolInfo(getMarket(), symbolId);
        info.setWindCode(windCode());
        info.setCnName(getCnName());
        info.setEnName(getEnName());
        return info;
    }

    public FutureItem(String symbolId) {
        this.symbolId = symbolId;
    }

    public void loadData(String file) {
        String[] arr = StringUtil.split(file, ',');

        if (arr.length < 2)
            return;

        tDate = Integer.parseInt(arr[0]);
        totalVolume = Long.parseLong(arr[1]);
    }

    public String writeData() {
        FixStringBuilder sb = new FixStringBuilder(',');
        sb.append(tDate);
        sb.append(totalVolume);
        return sb.toString();
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
