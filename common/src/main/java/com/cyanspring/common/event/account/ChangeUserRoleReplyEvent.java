package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeUserRoleReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private User user;
	private boolean isOk;
	private String message;
	public ChangeUserRoleReplyEvent(String key, String receiver,boolean isOk,String message,User user) {
		super(key, receiver);
		this.user = user;
		this.isOk = isOk;
		this.message = message;
	}
	
	public User getUser() {
		return user;
	}

	public boolean isOk() {
		return isOk;
	}

	public String getMessage() {
		return message;
	}
}
