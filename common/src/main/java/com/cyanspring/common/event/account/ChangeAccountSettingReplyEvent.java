package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeAccountSettingReplyEvent extends RemoteAsyncEvent {
	private AccountSetting accountSetting;
	private boolean ok;
	private String message;
	private Object infos[];
	public ChangeAccountSettingReplyEvent(String key, String receiver,
			AccountSetting accountSetting, boolean ok, String message,Object... infos) {
		super(key, receiver);
		this.accountSetting = accountSetting;
		this.ok = ok;
		this.message = message;
		this.infos = infos;
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

	public Object[] getInfos() {
		return infos;
	}
}
