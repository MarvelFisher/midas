package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ResetAccountReplyEvent extends RemoteAsyncEvent {
	private Account account;
	private String txId;
	private boolean ok;
	private String message;
	
	public ResetAccountReplyEvent(String key, String receiver, Account account,
			String txId, boolean ok, String message) {
		super(key, receiver);
		this.account = account;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
	}
	public Account getAccount() {
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
