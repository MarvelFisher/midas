package com.cyanspring.common.kdb;

import java.io.Serializable;
import java.sql.Time;

public class Volatility implements Serializable {

    private String symbol;

    private Time time;

    private double scale;

    public Volatility(String symbol, Time time, double scale) {
        this.symbol = symbol;
        this.time = time;
        this.scale = scale;
    }

    public String getSymbol() {
        return symbol;
    }

    public Time getTime() {
        return time;
    }

    public double getScale() {
        return scale;
    }

    @Override
    public String toString() {
        return "Volatility{" +
                "symbol='" + symbol + '\'' +
                ", time=" + time +
                ", scale=" + scale +
                '}';
    }
}
