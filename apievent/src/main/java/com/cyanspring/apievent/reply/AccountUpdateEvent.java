package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.ClientEvent;
import com.cyanspring.apievent.obj.Account;

public class AccountUpdateEvent extends ClientEvent {
	private Account account;

	public AccountUpdateEvent(String key, String receiver, Account account) {
		super(key, receiver);
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
	
}
