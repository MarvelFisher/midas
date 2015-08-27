package com.cyanspring.common.event.marketsession;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.MarketSessionData;

public class InternalSessionEvent extends RemoteAsyncEvent{
    private Map<String, MarketSessionData> dataMap;
    private boolean ok;
    public InternalSessionEvent(String key, String receiver, Map<String, MarketSessionData> dataMap, boolean ok) {
        super(key, receiver);
        this.dataMap = dataMap;
        this.ok = ok;
    }

    public Map<String, MarketSessionData> getDataMap() {
        return dataMap;
    }

    public boolean isOk() {
        return ok;
    }
}
