package com.cyanspring.server.livetrading.rule;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.livetrading.checker.LiveTradingCheckHandler;

public class CustomTrading implements IUserLiveTradingRule{

	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingCheckHandler.class);

    @Autowired
    AccountKeeper accountKeeper;

	@Override
	public AccountSetting setRule(AccountSetting oldAccountSetting, AccountSetting newAccountSetting)
			throws AccountException {

		AccountSetting tempSetting = oldAccountSetting;
		try {
			tempSetting = accountKeeper.getAccountSetting(newAccountSetting.getId());
			tempSetting.setStopLossPercent(newAccountSetting.getStopLossPercent());
			tempSetting.setFreezePercent(newAccountSetting.getFreezePercent());
			tempSetting.setTerminatePercent(newAccountSetting.getTerminatePercent());
			tempSetting.setLiveTrading(newAccountSetting.isLiveTrading());
			tempSetting.setUserLiveTrading(newAccountSetting.isUserLiveTrading());
			tempSetting.setLiveTradingType(LiveTradingType.CUSTOM);
			tempSetting.setLiveTradingSettedDate(TimeUtil.formatDate(TimeUtil.getOnlyDate(new Date()), dateFormat));
			tempSetting.setLtsApiPerm(newAccountSetting.isLtsApiPerm());
		} catch (AccountException e) {
			log.error(e.getMessage(),e);
		}

		return tempSetting;
	}

}
