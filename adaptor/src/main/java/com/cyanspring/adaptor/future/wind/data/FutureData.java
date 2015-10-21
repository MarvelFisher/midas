package com.cyanspring.adaptor.future.wind.data;

/**
 * Wind Future Data
 */
public class FutureData implements Cloneable{
    private String windCode;
    private String code;
    private int actionDay;
    private long[] askPrice;
    private long[] askVol;
    private long[] bidPrice;
    private long[] bidVol;
    private long match;
    private long close;
    private long high;
    private long highLimited;
    private long low;
    private long lowLimited;
    private long open;
    private long openInterest;
    private long preClose;
    private long preSettlePrice;
    private long settlePrice;
    private int status;
    private int time;
    private int tradingDay;
    private long turnover;
    private long volume;

    public int getActionDay() {
        return actionDay;
    }

    public void setActionDay(int actionDay) {
        this.actionDay = actionDay;
    }

    public long getClose() {
        return close;
    }

    public void setClose(long close) {
        this.close = close;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getHighLimited() {
        return highLimited;
    }

    public void setHighLimited(long highLimited) {
        this.highLimited = highLimited;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getLowLimited() {
        return lowLimited;
    }

    public void setLowLimited(long lowLimited) {
        this.lowLimited = lowLimited;
    }

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(long openInterest) {
        this.openInterest = openInterest;
    }

    public long getPreClose() {
        return preClose;
    }

    public void setPreClose(long preClose) {
        this.preClose = preClose;
    }

    public long getSettlePrice() {
        return settlePrice;
    }

    public void setSettlePrice(long settlePrice) {
        this.settlePrice = settlePrice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(int tradingDay) {
        this.tradingDay = tradingDay;
    }

    public long getTurnover() {
        return turnover;
    }

    public void setTurnover(long turnover) {
        this.turnover = turnover;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public String getWindCode() {
        return windCode;
    }

    public void setWindCode(String windCode) {
        this.windCode = windCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long[] getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(long[] askPrice) {
        this.askPrice = askPrice;
    }

    public long[] getAskVol() {
        return askVol;
    }

    public void setAskVol(long[] askVol) {
        this.askVol = askVol;
    }

    public long[] getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(long[] bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long[] getBidVol() {
        return bidVol;
    }

    public void setBidVol(long[] bidVol) {
        this.bidVol = bidVol;
    }

    public long getMatch() {
        return match;
    }

    public void setMatch(long match) {
        this.match = match;
    }

    public long getPreSettlePrice() {
        return preSettlePrice;
    }

    public void setPreSettlePrice(long preSettlePrice) {
        this.preSettlePrice = preSettlePrice;
    }

    public FutureData clone(){
        try {
            FutureData futureData = (FutureData)super.clone();
            return futureData;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
