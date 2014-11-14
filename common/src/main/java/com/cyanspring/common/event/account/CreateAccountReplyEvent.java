package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreateAccountReplyEvent extends RemoteAsyncEvent {
	private boolean ok;
	private String message; 
	private String txId;

	public CreateAccountReplyEvent(String key, String receiver, boolean ok,
			String message, String txId) {
		super(key, receiver);
		this.ok = ok;
		this.message = message;
		this.txId = txId;
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
