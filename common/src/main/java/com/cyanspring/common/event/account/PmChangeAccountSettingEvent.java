package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmChangeAccountSettingEvent extends RemoteAsyncEvent {
	private AccountSetting accountSetting;

	public PmChangeAccountSettingEvent(String key, String receiver,
			AccountSetting accountSetting) {
		super(key, receiver);
		this.accountSetting = accountSetting;
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}
}
