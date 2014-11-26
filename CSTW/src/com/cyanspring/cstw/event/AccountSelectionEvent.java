package com.cyanspring.cstw.event;

import com.cyanspring.common.event.AsyncEvent;

public class AccountSelectionEvent extends AsyncEvent {
	private String account;

	public AccountSelectionEvent(String account) {
		this.account = account;
	}

	public String getAccount() {
		return account;
	}
	
}
