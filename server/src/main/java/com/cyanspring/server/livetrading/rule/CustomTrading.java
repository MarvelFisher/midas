package com.cyanspring.server.livetrading.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.livetrading.LiveTradingException;
import com.cyanspring.server.livetrading.checker.LiveTradingCheckHandler;

public class CustomTrading implements IUserLiveTradingRule{
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingCheckHandler.class);
	
    @Autowired
    AccountKeeper accountKeeper;
	
	@Override
	public AccountSetting setRule(Account account, AccountSetting accountSetting)
			throws LiveTradingException {
		
		AccountSetting tempSetting = null;
		try {
			tempSetting = accountKeeper.getAccountSetting(account.getId());
			tempSetting.setStopLossPercent(accountSetting.getStopLossPercent());
			tempSetting.setFreezePercent(accountSetting.getFreezePercent());
			tempSetting.setTerminatePercent(accountSetting.getTerminatePercent());
			tempSetting.setUserLiveTrading(true);
		} catch (AccountException e) {
			log.error(e.getMessage(),e);
		}
		
		return tempSetting;
	}

}
