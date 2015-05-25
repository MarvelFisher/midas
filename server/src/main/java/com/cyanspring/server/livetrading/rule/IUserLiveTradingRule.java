package com.cyanspring.server.livetrading.rule;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;

public interface IUserLiveTradingRule {
	public static final String dateFormat = "yyyy-MM-dd";
	public AccountSetting setRule(AccountSetting oldAccountSetting,AccountSetting newAccountSetting)throws AccountException;
}
