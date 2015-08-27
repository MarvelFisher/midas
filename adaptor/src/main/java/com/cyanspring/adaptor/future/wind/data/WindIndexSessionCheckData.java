package com.cyanspring.adaptor.future.wind.data;

import com.cyanspring.common.Clock;
import com.cyanspring.common.marketsession.MarketSessionType;

import java.util.Date;

public class WindIndexSessionCheckData {
    private String index;
    private boolean sessionClose = false;
    private Date sessionCloseDate = Clock.getInstance().now();
    private int tradeDateForWindFormat = 0;

    public WindIndexSessionCheckData(String index){
        this.index = index;
    }

    public boolean isSessionClose() {
        return sessionClose;
    }

    public void setSessionClose(boolean sessionClose) {
        this.sessionClose = sessionClose;
    }

    public Date getSessionCloseDate() {
        return sessionCloseDate;
    }

    public void setSessionCloseDate(Date sessionCloseDate) {
        this.sessionCloseDate = sessionCloseDate;
    }

    public int getTradeDateForWindFormat() {
        return tradeDateForWindFormat;
    }

    public void setTradeDateForWindFormat(int tradeDateForWindFormat) {
        this.tradeDateForWindFormat = tradeDateForWindFormat;
    }
}
