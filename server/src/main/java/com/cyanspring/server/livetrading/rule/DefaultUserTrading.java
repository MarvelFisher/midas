package com.cyanspring.server.livetrading.rule;

import java.util.Date;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.util.TimeUtil;

public class DefaultUserTrading implements IUserLiveTradingRule{
	
	public Map <LiveTradingFieldType,Object> paramsMap;
	
	public DefaultUserTrading(Map <LiveTradingFieldType,Object>map ) {
		paramsMap = map;
	}
	@Override
	public AccountSetting setRule(AccountSetting oldAccountSetting, AccountSetting newAccountSetting) {
		
		AccountSetting setting = oldAccountSetting;	
		if(null == setting){
			setting = AccountSetting.createEmptySettings(oldAccountSetting.getId());
		}	
		System.out.println("DefaultUserTrading setRule");	
		double positionStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.POSITION_STOP_LOSS));
		double frozenStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS));
		double terminateStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS));
		setting.setStopLossPercent(positionStopLoss);
		setting.setFreezePercent(frozenStopLoss);
		setting.setTerminatePercent(terminateStopLoss);
		setting.setLiveTrading(newAccountSetting.isLiveTrading());
		setting.setUserLiveTrading(newAccountSetting.isUserLiveTrading());
		setting.setLiveTradingType(LiveTradingType.DEFAULT);
		setting.setLiveTradingSettedDate(TimeUtil.formatDate(TimeUtil.getOnlyDate(new Date()), dateFormat));
		System.out.printf("positionStopLoss:%s",positionStopLoss);
		return setting;
		
	}

}
