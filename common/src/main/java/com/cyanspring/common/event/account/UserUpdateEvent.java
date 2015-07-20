package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserUpdateEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private User user;
	public UserUpdateEvent(String key, String receiver,User user) {
		super(key, receiver);
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
