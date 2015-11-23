package com.cyanspring.common.event.account;

import com.cyanspring.common.event.AbstractReplyEvent;

public class MaxOrderQtyAllowedReplyEvent extends AbstractReplyEvent {
	private static final long serialVersionUID = 1L;
	private String account;
	private String symbol;
	private double qty;
	private double cashAvailable;

	public MaxOrderQtyAllowedReplyEvent(String key, String receiver,
			boolean ok, String txId, String msg, String account, String symbol,
			double qty, double cashAvailable) {
		super(key, receiver, ok, txId, msg);
		this.account = account;
		this.symbol = symbol;
		this.qty = qty;
		this.cashAvailable = cashAvailable;
	}

	public String getAccount() {
		return account;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getQty() {
		return qty;
	}

	public double getCashAvailable() {
		return cashAvailable;
	}
}
