package com.cyanspring.common.event.order;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * This event is used to cancel pending orders at specific time
 * Subscriber: BusinessManager.java
 * Invoker: BusinessManager.java
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class CancelPendingOrderEvent extends RemoteAsyncEvent{
    public CancelPendingOrderEvent(String key, String receiver) {
        super(key, receiver);
    }
}
