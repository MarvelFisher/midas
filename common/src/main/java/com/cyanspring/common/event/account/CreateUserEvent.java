package com.cyanspring.common.event.account;

import com.cyanspring.common.account.User;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreateUserEvent extends RemoteAsyncEvent {
	private User user;
	private String txId;

	public CreateUserEvent(String key, String receiver, User user, String txId) {
		super(key, receiver);
		this.user = user;
		this.txId = txId;
	}

	public User getUser() {
		return user;
	}

	public String getTxId() {
		return txId;
	}
	
}
