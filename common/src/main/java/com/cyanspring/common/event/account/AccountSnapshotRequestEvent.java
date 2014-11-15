package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountSnapshotRequestEvent extends RemoteAsyncEvent {
	private String accountId;

	public AccountSnapshotRequestEvent(String key, String receiver,
			String accountId) {
		super(key, receiver);
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
	
}
