package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public final class CancelParentOrderEvent extends RemoteAsyncEvent {
	private String orderId;
	private String txId;
	private boolean force;
	public CancelParentOrderEvent(String key, String receiver, String orderId, boolean force,
								  String txId) {
		super(key, receiver);
		this.orderId = orderId;
		this.txId = txId;
		this.force = force;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isForce() {
		return force;
	}
	
}
