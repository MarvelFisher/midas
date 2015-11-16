package com.cyanspring.common.kdb;

import java.io.Serializable;
import java.util.Date;

public class MarketIntelligence implements Serializable {

    private int timeIntervalSeconds;

    private String symbol;

    private Date time;

    private MarketIntelligenceIndex relativeVolatility;

    private MarketIntelligenceIndex currentVolatility;

    private MarketIntelligenceIndex momentum;

    private MarketIntelligenceIndex ultimateMomentumBuy;

    private MarketIntelligenceIndex ultimateMomentumSell;

    public MarketIntelligence(int timeIntervalSeconds, String symbol, Date time, MarketIntelligenceIndex relativeVolatility, MarketIntelligenceIndex currentVolatility, MarketIntelligenceIndex momentum, MarketIntelligenceIndex ultimateMomentumBuy, MarketIntelligenceIndex ultimateMomentumSell) {
        this.timeIntervalSeconds = timeIntervalSeconds;
        this.symbol = symbol;
        this.time = time;
        this.relativeVolatility = relativeVolatility;
        this.currentVolatility = currentVolatility;
        this.momentum = momentum;
        this.ultimateMomentumBuy = ultimateMomentumBuy;
        this.ultimateMomentumSell = ultimateMomentumSell;
    }

    public int getTimeIntervalSeconds() {
        return timeIntervalSeconds;
    }

    public String getSymbol() {
        return symbol;
    }

    public Date getTime() {
        return time;
    }

    public MarketIntelligenceIndex getRelativeVolatility() {
        return relativeVolatility;
    }

    public MarketIntelligenceIndex getCurrentVolatility() {
        return currentVolatility;
    }

    public MarketIntelligenceIndex getMomentum() {
        return momentum;
    }

    public MarketIntelligenceIndex getUltimateMomentumBuy() {
        return ultimateMomentumBuy;
    }

    public MarketIntelligenceIndex getUltimateMomentumSell() {
        return ultimateMomentumSell;
    }

    @Override
    public String toString() {
        return "MarketIntelligence{" +
                "timeIntervalSeconds=" + timeIntervalSeconds +
                ", symbol='" + symbol + '\'' +
                ", time=" + time +
                ", relativeVolatility=" + relativeVolatility +
                ", currentVolatility=" + currentVolatility +
                ", momentum=" + momentum +
                ", ultimateMomentumBuy=" + ultimateMomentumBuy +
                ", ultimateMomentumSell=" + ultimateMomentumSell +
                '}';
    }
}
