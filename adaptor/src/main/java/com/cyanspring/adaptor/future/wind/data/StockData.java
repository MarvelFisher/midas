package com.cyanspring.adaptor.future.wind.data;

/**
 * Wind Stock Data
 */
public class StockData {
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
    private int IOPV;
    private long preClose;
    private int status;
    private int time;
    private int tradingDay;
    private long turnover;
    private long volume;
    private long numTrades;
    private long totalBidVol;
    private long totalAskVol;
    private long WeightedAvgAskPrice;
    private long WeightedAvgBidPrice;
    private int yieldToMaturity;
    private String prefix;
    private int syl1;
    private int syl2;
    private int SD2;
    private long buyVol;
    private long sellVol;
    private long unclassifiedVol;
    private long buyTurnover;
    private long sellTurnover;
    private long unclassifiedTurnover;

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

    public int getIOPV() {
        return IOPV;
    }

    public void setIOPV(int IOPV) {
        this.IOPV = IOPV;
    }

    public long getPreClose() {
        return preClose;
    }

    public void setPreClose(long preClose) {
        this.preClose = preClose;
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

    public long getNumTrades() {
        return numTrades;
    }

    public void setNumTrades(long numTrades) {
        this.numTrades = numTrades;
    }

    public long getTotalBidVol() {
        return totalBidVol;
    }

    public void setTotalBidVol(long totalBidVol) {
        this.totalBidVol = totalBidVol;
    }

    public long getTotalAskVol() {
        return totalAskVol;
    }

    public void setTotalAskVol(long totalAskVol) {
        this.totalAskVol = totalAskVol;
    }

    public long getWeightedAvgAskPrice() {
        return WeightedAvgAskPrice;
    }

    public void setWeightedAvgAskPrice(long weightedAvgAskPrice) {
        this.WeightedAvgAskPrice = weightedAvgAskPrice;
    }

    public int getYieldToMaturity() {
        return yieldToMaturity;
    }

    public void setYieldToMaturity(int yieldToMaturity) {
        this.yieldToMaturity = yieldToMaturity;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getSyl1() {
        return syl1;
    }

    public void setSyl1(int syl1) {
        this.syl1 = syl1;
    }

    public int getSyl2() {
        return syl2;
    }

    public void setSyl2(int syl2) {
        this.syl2 = syl2;
    }

    public int getSD2() {
        return SD2;
    }

    public void setSD2(int SD2) {
        this.SD2 = SD2;
    }

    public long getWeightedAvgBidPrice() {
        return WeightedAvgBidPrice;
    }

    public void setWeightedAvgBidPrice(long weightedAvgBidPrice) {
        WeightedAvgBidPrice = weightedAvgBidPrice;
    }

    public long getBuyVol() {
        return buyVol;
    }

    public void setBuyVol(long buyVol) {
        this.buyVol = buyVol;
    }

    public long getSellVol() {
        return sellVol;
    }

    public void setSellVol(long sellVol) {
        this.sellVol = sellVol;
    }

    public long getBuyTurnover() {
        return buyTurnover;
    }

    public void setBuyTurnover(long buyTurnover) {
        this.buyTurnover = buyTurnover;
    }

    public long getSellTurnover() {
        return sellTurnover;
    }

    public void setSellTurnover(long sellTurnover) {
        this.sellTurnover = sellTurnover;
    }

    public long getUnclassifiedVol() {
        return unclassifiedVol;
    }

    public void setUnclassifiedVol(long unclassifiedVol) {
        this.unclassifiedVol = unclassifiedVol;
    }

    public long getUnclassifiedTurnover() {
        return unclassifiedTurnover;
    }

    public void setUnclassifiedTurnover(long unclassifiedTurnover) {
        this.unclassifiedTurnover = unclassifiedTurnover;
    }
}
