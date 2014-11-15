package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountSettingSnapshotRequestEvent extends RemoteAsyncEvent {
	String accountId;

	public AccountSettingSnapshotRequestEvent(String key, String receiver,
			String accountId) {
		super(key, receiver);
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
