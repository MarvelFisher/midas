package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmCreateUserEvent extends RemoteAsyncEvent {
	private User user;
	private List<Account> accounts;
	static private List<Account> EMPTY_ACCOUNT = new ArrayList<Account>();

	public PmCreateUserEvent(String key, String receiver, User user, List<Account> accounts) {
		super(key, receiver);
		this.user = user;
		this.accounts = accounts;
	}
	
	public PmCreateUserEvent(String key, String receiver, User user) {
		super(key, receiver);
		this.user = user;
		this.accounts = EMPTY_ACCOUNT;
	}

	public User getUser() {
		return user;
	}
	public List<Account> getAccounts()
	{
		return accounts;
	}
}
