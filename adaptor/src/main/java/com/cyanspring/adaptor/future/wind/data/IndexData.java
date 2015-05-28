package com.cyanspring.adaptor.future.wind.data;

public class IndexData {
    private String windCode;
    private String code;
    private int actionDay;
    private long highIndex;
    private long lastIndex;
    private long lowIndex;
    private long openIndex;
    private long prevIndex;
    private int time;
    private long totalVolume;
    private int tradingDay;
    private long turnover;

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

    public int getActionDay() {
        return actionDay;
    }

    public void setActionDay(int actionDay) {
        this.actionDay = actionDay;
    }

    public long getHighIndex() {
        return highIndex;
    }

    public void setHighIndex(long highIndex) {
        this.highIndex = highIndex;
    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public long getLowIndex() {
        return lowIndex;
    }

    public void setLowIndex(long lowIndex) {
        this.lowIndex = lowIndex;
    }

    public long getOpenIndex() {
        return openIndex;
    }

    public void setOpenIndex(long openIndex) {
        this.openIndex = openIndex;
    }

    public long getPrevIndex() {
        return prevIndex;
    }

    public void setPrevIndex(long prevIndex) {
        this.prevIndex = prevIndex;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(long totalVolume) {
        this.totalVolume = totalVolume;
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
}
