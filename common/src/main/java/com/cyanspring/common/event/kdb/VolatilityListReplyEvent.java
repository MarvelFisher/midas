package com.cyanspring.common.event.kdb;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.kdb.Volatility;

import java.util.List;

public class VolatilityListReplyEvent extends RemoteAsyncEvent {

    private List<Volatility> volatilities;

    public VolatilityListReplyEvent(String key, String receiver, List<Volatility> volatilities) {
        super(key, receiver);
        this.volatilities = volatilities;
    }

    public List<Volatility> getVolatilities() {
        return volatilities;
    }
}
