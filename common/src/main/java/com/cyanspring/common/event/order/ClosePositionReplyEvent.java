package com.cyanspring.common.event.order;

public class ClosePositionReplyEvent {
	private String account;
	private String symbol;
	private String txId;
	private boolean ok;
	private String message;
	public ClosePositionReplyEvent(String account, String symbol, String txId,
			boolean ok, String message) {
		super();
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
