package com.cyanspring.server.livetrading.checker;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;

public class FrozenStopLossCheck implements ILiveTradingChecker {

	@Override
	public boolean check(Account account, AccountSetting accountSetting) {
		System.out.println("into FrozenStopLossCheck");
		return false;
	}

}
