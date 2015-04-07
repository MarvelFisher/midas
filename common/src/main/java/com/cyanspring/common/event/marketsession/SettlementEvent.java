package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.RefData;

public class SettlementEvent extends RemoteAsyncEvent {

    private String symbol;
    public SettlementEvent(String key, String receiver, String symbol) {
        super(key, receiver);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
