package com.cyanspring.common.event.kdb;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class VolatilityListRequestEvent extends RemoteAsyncEvent {

    public VolatilityListRequestEvent(String key, String receiver) {
        super(key, receiver);
    }

}
