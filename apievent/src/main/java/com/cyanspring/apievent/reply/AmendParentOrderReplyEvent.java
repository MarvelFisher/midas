package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class AmendParentOrderReplyEvent extends ParentOrderReplyEvent {
    public AmendParentOrderReplyEvent(String key, String receiver, boolean ok, String message, String txId, Order order) {
        super(key, receiver, ok, message, txId, order);
    }
}
