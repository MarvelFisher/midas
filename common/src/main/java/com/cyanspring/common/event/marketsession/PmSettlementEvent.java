package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmSettlementEvent extends RemoteAsyncEvent{
    private SettlementEvent event;
    public PmSettlementEvent(String key, String receiver, SettlementEvent event) {
        super(key, receiver);
        this.event = event;
    }

    public SettlementEvent getEvent() {
        return event;
    }
}
