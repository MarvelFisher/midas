package com.cyanspring.common.event.livetrading;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class LiveTradingAccountSettingRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private LiveTradingType liveTradingType ;
	private Account account ;
	private AccountSetting accountSetting;
	
	public LiveTradingAccountSettingRequestEvent(String key, String receiver,LiveTradingType type,Account account,AccountSetting accountSetting) {
		super(key, receiver);
		liveTradingType = type;
		this.account = account;
		this.accountSetting = accountSetting;
	}

	public LiveTradingType getLiveTradingType() {
		return liveTradingType;
	}

	public Account getAccount() {
		return account;
	}

	public AccountSetting getAccountSetting() {
		return accountSetting;
	}

}
