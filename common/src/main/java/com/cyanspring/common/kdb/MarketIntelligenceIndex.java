package com.cyanspring.common.kdb;

import java.io.Serializable;
import java.util.Date;

public class MarketIntelligenceIndex implements Serializable {

    private double value;

    private double max;

    private double min;

    public MarketIntelligenceIndex(double value, double max, double min) {
        this.value = value;
        this.max = max;
        this.min = min;
    }

    public double getValue() {
        return value;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    @Override
    public String toString() {
        return "MarketIntelligenceIndex{" +
                "value=" + value +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}
