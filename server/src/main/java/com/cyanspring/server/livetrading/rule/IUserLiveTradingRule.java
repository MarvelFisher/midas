package com.cyanspring.server.livetrading.rule;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.server.livetrading.LiveTradingException;

public interface IUserLiveTradingRule {
	public AccountSetting setRule(Account account,AccountSetting accountSetting)throws LiveTradingException;
}
