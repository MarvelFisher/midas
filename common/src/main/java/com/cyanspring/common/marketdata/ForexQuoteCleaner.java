package com.cyanspring.common.marketdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.type.QtyPrice;

/**
 * Created by Shuwei.kuo on 15/7/28.
 */
public class ForexQuoteCleaner implements IQuoteCleaner{

    @Override
    public Quote clear(Quote quote) {
        quote.setAsk(0);
        quote.setAskVol(0);
        quote.setBid(0);
        quote.setBidVol(0);
        quote.setOpen(0);
        quote.setHigh(0);
        quote.setLow(0);
        quote.setLast(0);
        quote.setLastVol(0);
        quote.setTurnover(0);
        quote.setTotalVolume(0);
        quote.setTimeStamp(Clock.getInstance().now());
        if(null != quote.getBids() && quote.getBids().size() > 0){
            for(QtyPrice qtyPrice : quote.getBids()){
                qtyPrice.setPrice(0);
                qtyPrice.setQuantity(0);
            }
        }
        if(null != quote.getAsks() && quote.getAsks().size() > 0){
            for(QtyPrice qtyPrice : quote.getAsks()){
                qtyPrice.setPrice(0);
                qtyPrice.setQuantity(0);
            }
        }
        return quote;
    }
}
