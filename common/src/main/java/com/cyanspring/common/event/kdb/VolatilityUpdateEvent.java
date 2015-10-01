package com.cyanspring.common.event.kdb;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.kdb.Volatility;

import java.util.List;

public class VolatilityUpdateEvent extends RemoteAsyncEvent {

    private List<Volatility> volatilities;

    public VolatilityUpdateEvent(String key, String receiver, List<Volatility> volatilities) {
        super(key, receiver);
        this.volatilities = volatilities;
    }

    public List<Volatility> getVolatilities() {
        return volatilities;
    }

    @Override
    public String toString() {
        return "VolatilityUpdateEvent{" +
                "volatilities=" + volatilities +
                '}';
    }
}
