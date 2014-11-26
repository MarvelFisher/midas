package com.cyanspring.cstw.event;

import com.cyanspring.common.event.AsyncEvent;

public class SelectUserAccountEvent extends AsyncEvent {
	private String User;
	private String Account;
	
	public SelectUserAccountEvent(String user, String account) {
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
