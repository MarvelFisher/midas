package com.cyanspring.adaptor.future.wind.data;

public class TransationData implements Cloneable{
    private String windCode;
    private int actionDay;
    private int time;
    private long match;
    private int indexNumber;
    private long volume;
    private long turnover;
    private int buySellFlag;

    public String getWindCode() {
        return windCode;
    }

    public void setWindCode(String windCode) {
        this.windCode = windCode;
    }

    public int getActionDay() {
        return actionDay;
    }

    public void setActionDay(int actionDay) {
        this.actionDay = actionDay;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getMatch() {
        return match;
    }

    public void setMatch(long match) {
        this.match = match;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public long getTurnover() {
        return turnover;
    }

    public void setTurnover(long turnover) {
        this.turnover = turnover;
    }

    public int getBuySellFlag() {
        return buySellFlag;
    }

    public void setBuySellFlag(int buySellFlag) {
        this.buySellFlag = buySellFlag;
    }

    public TransationData clone(){
        try {
            TransationData transationData = (TransationData)super.clone();
            return transationData;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
