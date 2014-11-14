package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountUpdateEvent extends RemoteAsyncEvent {
	private Account account;

	public AccountUpdateEvent(String key, String receiver, Account account) {
		super(key, receiver);
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
	
}
