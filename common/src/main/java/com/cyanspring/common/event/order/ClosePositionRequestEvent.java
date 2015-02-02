package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClosePositionRequestEvent extends RemoteAsyncEvent {
	private String account;
	private String symbol;
	private double qty;
	private OrderReason reason;
	private String txId;
	
	public ClosePositionRequestEvent(String key, String receiver,
			String account, String symbol, double qty, OrderReason reason, String txId) {
		super(key, receiver);
		this.account = account;
		this.symbol = symbol;
		this.qty = qty;
		this.reason = reason;
		this.txId = txId;
	}

	public String getAccount() {
		return account;
	}

	public String getSymbol() {
		return symbol;
	}

	public OrderReason getReason() {
		return reason;
	}
	
	public String getTxId() {
		return txId;
	}

	public double getQty() {
		return qty;
	}
	
}
