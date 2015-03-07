package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ResetAccountReplyEvent extends RemoteAsyncEvent {
	private String account;
	private String txId;
	private boolean ok;
	private String message;
	
	public ResetAccountReplyEvent(String key, String receiver, String account,
			String txId, boolean ok, String message) {
		super(key, receiver);
		this.account = account;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
	}
	public String getAccount() {
		return account;
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
