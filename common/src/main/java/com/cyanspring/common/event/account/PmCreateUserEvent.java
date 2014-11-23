package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmCreateUserEvent extends RemoteAsyncEvent {
	private User user;
	private List<Account> accounts;
	private CreateUserEvent event;
	static private List<Account> EMPTY_ACCOUNT = new ArrayList<Account>();

	public PmCreateUserEvent(String key, String receiver, User user, CreateUserEvent event, List<Account> accounts) {
		super(key, receiver);
		this.user = user;
		this.accounts = accounts;
		this.event = event;
	}
	
	public PmCreateUserEvent(String key, String receiver, User user, CreateUserEvent event) {
		super(key, receiver);
		this.user = user;
		this.event = event;
		this.accounts = EMPTY_ACCOUNT;
	}

	public User getUser() 
	{
		return user;
	}
	public List<Account> getAccounts()
	{
		return accounts;
	}
	public CreateUserEvent getOriginalEvent()
	{
		return event;
	}
}
