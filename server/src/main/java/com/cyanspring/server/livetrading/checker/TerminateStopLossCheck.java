package com.cyanspring.server.livetrading.checker;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;

public class TerminateStopLossCheck implements ILiveTradingChecker {

	@Override
	public boolean check(Account account, AccountSetting accountSetting) {
		System.out.println("into TerminateStopLossCheck");
		return false;
	}

}
