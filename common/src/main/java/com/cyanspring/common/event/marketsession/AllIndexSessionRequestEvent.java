package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * This event is used to request all detail marketSession data in a map
 * Subscriber: IndexMarketSessionManager.java
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class AllIndexSessionRequestEvent extends RemoteAsyncEvent{

    public AllIndexSessionRequestEvent(String key, String receiver) {
        super(key, receiver);
    }
}
