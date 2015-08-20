package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public abstract class ParentOrderReplyEvent extends RemoteAsyncEvent {
	private boolean ok;
	private String message;
	private String txId;
	private Order order;
	
	public ParentOrderReplyEvent(String key, String receiver, boolean ok,
								 String message, String txId, Order order) {
		super(key, receiver);
		this.ok = ok;
		this.message = message;
		this.txId = txId;
		this.order = order;
	}

	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}

	public String getTxId() {
		return txId;
	}

	public Order getOrder() {
		return order;
	}

}
