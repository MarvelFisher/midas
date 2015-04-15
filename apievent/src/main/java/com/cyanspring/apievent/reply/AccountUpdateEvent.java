package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Account;
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
