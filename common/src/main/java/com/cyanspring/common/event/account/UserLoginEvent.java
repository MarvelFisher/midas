package com.cyanspring.common.event.account;

import com.cyanspring.common.account.UserLoginType;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public final class UserLoginEvent extends RemoteAsyncEvent {
	private String userId;
	private String password;
	private String txId;
	private UserLoginType loginType;

	public UserLoginEvent(String key, String receiver, String userId,
						  String password, String txId) {
		this(key, receiver, userId, password, txId, UserLoginType.USER_ID);
	}

	public UserLoginEvent(String key, String receiver, String userId,
			String password, String txId, UserLoginType loginType) {
		super(key, receiver);
		this.userId = userId;
		this.password = password;
		this.txId = txId;
		this.loginType = loginType;
		setPriority(EventPriority.HIGH);
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

	public UserLoginType getLoginType() {
		return loginType;
	}
}
