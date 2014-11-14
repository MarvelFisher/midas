package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public final class UserLoginEvent extends RemoteAsyncEvent {
	private String userId;
	private String password;
	private String txId;
	
	public UserLoginEvent(String key, String receiver, String userId,
			String password, String txId) {
		super(key, receiver);
		this.userId = userId;
		this.password = password;
		this.txId = txId;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getTxId() {
		return txId;
	}
	
}
