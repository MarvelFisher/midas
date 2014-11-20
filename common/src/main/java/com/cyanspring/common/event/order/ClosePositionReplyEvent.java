package com.cyanspring.common.event.order;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClosePositionReplyEvent extends RemoteAsyncEvent {
	private String account;
	private String symbol;
	private String txId;
	private boolean ok;
	private String message;
	
	public ClosePositionReplyEvent(String key, String receiver, String account,
			String symbol, String txId, boolean ok, String message) {
		super(key, receiver);
		this.account = account;
		this.symbol = symbol;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
	}

	public String getAccount() {
		return account;
	}
	public String getSymbol() {
		return symbol;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}
	
	
}
