package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountSettingSnapshotReplyEvent extends RemoteAsyncEvent {
	private AccountSetting accountSetting;
	private boolean ok;
	private String message;
	
	public AccountSettingSnapshotReplyEvent(String key, String receiver,
			AccountSetting accountSetting, boolean ok, String message) {
		super(key, receiver);
		this.accountSetting = accountSetting;
		this.ok = ok;
		this.message = message;
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}

	public boolean isOk() {
		return ok;
	}

	public String getMessage() {
		return message;
	}

}
