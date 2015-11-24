package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

public class SelectUserAccountLocalEvent extends AsyncEvent {
	private String User;
	private String Account;
	
	public SelectUserAccountLocalEvent(String user, String account) {
		super();
		User = user;
		Account = account;
	}
	public String getUser() {
		return User;
	}
	public String getAccount() {
		return Account;
	}
	
}
