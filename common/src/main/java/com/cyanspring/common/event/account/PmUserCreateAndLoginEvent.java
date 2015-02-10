package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmUserCreateAndLoginEvent extends RemoteAsyncEvent {
	private User user;
	private List<Account> accounts;
	static private List<Account> EMPTY_ACCOUNT = new ArrayList<Account>();
	Object m_userKeeper;
	Object m_accountKeeper;
	UserCreateAndLoginEvent m_originalEvent;
	
	public Object getUserKeeper()
	{
		return m_userKeeper;
	}
	public Object getAccountKeeper()
	{
		return m_accountKeeper;
	}
	public UserCreateAndLoginEvent getOriginalEvent()
	{
		return m_originalEvent;
	}

	public PmUserCreateAndLoginEvent(String key, String receiver, User user, UserCreateAndLoginEvent event, Object userKeeper, Object accountKeeper, List<Account> accounts) {
		super(key, receiver);
		this.user = user;
		this.accounts = accounts;
		this.m_originalEvent = event;
		m_userKeeper = userKeeper;
		m_accountKeeper = accountKeeper;
	}
	
	public PmUserCreateAndLoginEvent(String key, String receiver, User user, UserCreateAndLoginEvent event, Object userKeeper, Object accountKeeper) {
		super(key, receiver);
		this.user = user;
		this.m_originalEvent = event;
		this.accounts = EMPTY_ACCOUNT;
		m_userKeeper = userKeeper;
		m_accountKeeper = accountKeeper;
	}

	public User getUser() 
	{
		return user;
	}
	public List<Account> getAccounts()
	{
		return accounts;
	}

}
