package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.AsyncEvent;

public class OnUserCreatedEvent extends AsyncEvent {
	private User user;
	private List<Account> accounts;
	
	public OnUserCreatedEvent(User user, List<Account> accounts) {
		super();
		this.user = user;
		this.accounts = accounts;
	}

	public User getUser() {
		return user;
	}

	public List<Account> getAccounts() {
		return accounts;
	}
	
}
