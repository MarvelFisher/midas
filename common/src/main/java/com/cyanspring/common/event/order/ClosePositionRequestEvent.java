package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OrderReason;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClosePositionRequestEvent extends RemoteAsyncEvent {
	private String account;
	private String symbol;
	private double qty;
	private OrderReason reason;
	private String txId;
	private boolean isFromSystem;
	
	public ClosePositionRequestEvent(String key, String receiver,
			String account, String symbol, double qty, OrderReason reason, String txId) {
		this(key,receiver,account,symbol,qty,reason,txId,false);
	}
	
	public ClosePositionRequestEvent(String key, String receiver,
			String account, String symbol, double qty, OrderReason reason, String txId,boolean isFromSystem) {
		super(key, receiver);
		this.account = account;
		this.symbol = symbol;
		this.qty = qty;
		this.reason = reason;
		this.txId = txId;
		this.isFromSystem = isFromSystem;
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

	public boolean isFromSystem() {
		return isFromSystem;
	}
}
