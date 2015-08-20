package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;
import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class StrategySnapshotEvent extends RemoteAsyncEvent {
	private List<Order> orders;
	private String txId;

	public StrategySnapshotEvent(String key, String receiver,
								 List<Order> orders,
								 String txId) {
		super(key, receiver);
		this.orders = orders;
		this.txId = txId;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public String getTxId() {
		return txId;
	}
	
}
