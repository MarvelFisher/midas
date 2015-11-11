package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmCreateCSTWUserEvent extends RemoteAsyncEvent{
	private User user;
	private CreateUserEvent event;

	public PmCreateCSTWUserEvent(String key, String receiver, User user, CreateUserEvent event) {
		super(key, receiver);
		this.user = user;
		this.event = event;
	}

	public User getUser() 
	{
		return user;
	}

	public CreateUserEvent getOriginalEvent()
	{
		return event;
	}
}
