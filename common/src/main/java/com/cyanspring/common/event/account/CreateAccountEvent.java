package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreateAccountEvent extends RemoteAsyncEvent {
	private Account account;
	private String txId;
	
	public CreateAccountEvent(String key, String receiver, Account account,
			String txId) {
		super(key, receiver);
		this.account = account;
		this.txId = txId;
	}
	public Account getAccount() {
		return account;
	}
	public String getTxId() {
		return txId;
	}
}
