package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.MarketSession;
import com.cyanspring.common.marketsession.MarketSessionData;

import java.util.Map;

/**
 * This event is used to response detail marketSession data
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class AllIndexSessionEvent extends RemoteAsyncEvent {

    private Map<String, MarketSessionData> map;
    public AllIndexSessionEvent(String key, String receiver, Map<String, MarketSessionData> map) {
        super(key, receiver);
        this.map = map;
    }

    public Map<String, MarketSessionData> getMap() {
        return map;
    }
}
