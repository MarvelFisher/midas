package com.cyanspring.common.event.account;

import com.cyanspring.common.event.AbstractReplyEvent;

public class MaxOrderQtyAllowedReplyEvent extends AbstractReplyEvent {
	private static final long serialVersionUID = 1L;
	private double qty;
	private double cashAvailable;

	public MaxOrderQtyAllowedReplyEvent(String key, String receiver,
			boolean ok, String txId, String msg, double qty,
			double cashAvailable) {
		super(key, receiver, ok, txId, msg);
		this.qty = qty;
		this.cashAvailable = cashAvailable;
	}

	public double getQty() {
		return qty;
	}

	public double getCashAvailable() {
		return cashAvailable;
	}
}
