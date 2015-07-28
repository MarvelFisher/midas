package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;

/**
 * Created by Shuwei.kuo on 15/7/28.
 */
public class WindQuoteCleaner implements IQuoteCleaner{
    @Override
    public Quote clear(Quote quote) {
        quote.setAsk(0);
        quote.setAskVol(0);
        quote.setBid(0);
        quote.setBidVol(0);
        quote.setHigh(0);
        quote.setLow(0);
        quote.setLast(0);
        quote.setLastVol(0);
        quote.setTurnover(0);
        quote.setTotalVolume(0);
        quote.setTimeStamp(Clock.getInstance().now());
        if(null != quote.getBids() && quote.getBids().size() > 0) quote.getBids().clear();
        if(null != quote.getAsks() && quote.getAsks().size() > 0) quote.getAsks().clear();
        return quote;
    }
}
