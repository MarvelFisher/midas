package com.cyanspring.server.livetrading.checker;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;

public interface ILiveTradingChecker {
	public boolean check(Account account,AccountSetting accountSetting);
}
