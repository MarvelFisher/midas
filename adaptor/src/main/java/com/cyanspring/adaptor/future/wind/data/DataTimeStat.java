package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.adaptor.future.wind.WindDef;
import com.cyanspring.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DataTimeStat {

    private static final Logger log = LoggerFactory
            .getLogger(DataTimeStat.class);

    private String symbol;
    private long quoteReceiveCount = 0;
    private long quoteOverTimeCount = 0;
    private long maxOverTime;

    public DataTimeStat(String symbol){
        this.symbol = symbol;
    }

    public void processReceiveQuoteTime(Date tickTime){
        quoteReceiveCount++;
        long overTime = TimeUtil.getTimePass(tickTime);
        if(overTime > WindDef.STOCK_WARNING_MILLISECONDS) {
            quoteOverTimeCount++;
            if(overTime > maxOverTime) {
                maxOverTime = overTime;
                log.debug(symbol+" Come OverTime " + maxOverTime);
            }
        }
    }

    public void printStat(){
        log.debug("Symbol=" + symbol + ",max=" + maxOverTime + ",QRC=" + quoteReceiveCount + ",QOTC=" + quoteOverTimeCount);
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
