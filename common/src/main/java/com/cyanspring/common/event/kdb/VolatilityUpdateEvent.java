package com.cyanspring.common.event.kdb;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.kdb.Volatility;

public class VolatilityUpdateEvent extends RemoteAsyncEvent {

    private Volatility volatility;

    public VolatilityUpdateEvent(String key, String receiver, Volatility volatility) {
        super(key, receiver);
        this.volatility = volatility;
    }

    public Volatility getVolatility() {
        return volatility;
    }

    @Override
    public String toString() {
        return "VolatilityUpdateEvent{" +
                "volatility=" + volatility +
                '}';
    }
}
