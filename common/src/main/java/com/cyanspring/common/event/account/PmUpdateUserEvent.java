package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmUpdateUserEvent extends RemoteAsyncEvent {
	private User user;

	public PmUpdateUserEvent(String key, String receiver, User user) {
		super(key, receiver);
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	

}
