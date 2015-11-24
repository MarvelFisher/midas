package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

public class AccountSelectionLocalEvent extends AsyncEvent {
	private String account;

	public AccountSelectionLocalEvent(String account) {
		this.account = account;
	}

	public String getAccount() {
		return account;
	}
	
}
