package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class ParentOrderUpdateEvent extends RemoteAsyncEvent {
    private String txId;
    private Order order;
    private String info;

    public ParentOrderUpdateEvent(String key, String receiver,
                                  String txId, Order order, String info) {
        super(key, receiver);
        this.txId = txId;
        this.order = order;
        this.info = info;
    }

    public Order getOrder() {
        return order;
    }

    public String getTxId() {
        return txId;
    }

    public String getInfo() {
        return info;
    }

}
