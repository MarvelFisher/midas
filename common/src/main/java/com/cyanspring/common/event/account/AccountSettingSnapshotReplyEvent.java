package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountSettingSnapshotReplyEvent extends RemoteAsyncEvent {
	private AccountSetting accountSetting;
	private boolean ok;
	private String message;
	private String txId;
	private Object infos[];
	
	public AccountSettingSnapshotReplyEvent(String key, String receiver,
			AccountSetting accountSetting, boolean ok, String message, String txId,Object... infos) {
		super(key, receiver);
		this.accountSetting = accountSetting;
		this.ok = ok;
		this.message = message;
		this.txId = txId;
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

	public String getTxId() {
		return txId;
	}

	public Object[] getInfos() {
		return infos;
	}
	
}
