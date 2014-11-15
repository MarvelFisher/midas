package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreateUserReplyEvent extends RemoteAsyncEvent {
	private User user;
	private boolean ok;
	private String message; 
	private String txId;

	public CreateUserReplyEvent(String key, String receiver, User user,
			boolean ok, String message, String txId) {
		super(key, receiver);
		this.user = user;
		this.ok = ok;
		this.message = message;
		this.txId = txId;
	}

	public String getTxId() {
		return txId;
	}

	public User getUser() {
		return user;
	}

	public boolean isOk() {
		return ok;
	}

	public String getMessage() {
		return message;
	}
	
}