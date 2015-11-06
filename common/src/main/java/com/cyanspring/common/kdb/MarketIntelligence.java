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

    private MarketIntelligenceIndex ultimateMomentum;

    public MarketIntelligence(int timeIntervalSeconds, String symbol, Date time, MarketIntelligenceIndex relativeVolatility, MarketIntelligenceIndex currentVolatility, MarketIntelligenceIndex momentum, MarketIntelligenceIndex ultimateMomentum) {
        this.timeIntervalSeconds = timeIntervalSeconds;
        this.symbol = symbol;
        this.time = time;
        this.relativeVolatility = relativeVolatility;
        this.currentVolatility = currentVolatility;
        this.momentum = momentum;
        this.ultimateMomentum = ultimateMomentum;
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

    public MarketIntelligenceIndex getUltimateMomentum() {
        return ultimateMomentum;
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
                ", ultimateMomentum=" + ultimateMomentum +
                '}';
    }
}
