package com.cyanspring.server.livetrading.rule;

import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;

public class DefaultUserTrading implements IUserLiveTradingRule{
	
	public Map <LiveTradingFieldType,Object> paramsMap;
	
	public DefaultUserTrading(Map <LiveTradingFieldType,Object>map ) {
		paramsMap = map;
	}
	@Override
	public AccountSetting setRule(Account account,AccountSetting accountSetting) {
		
		AccountSetting setting = null;	
		if(null == accountSetting){
			setting = AccountSetting.createEmptySettings(account.getId());
		}
		
		System.out.println("DefaultUserTrading setRule");	
		double positionStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.POSTION_STOP_LOSS));
		double frozenStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS));
		double terminateStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS));
		accountSetting.setStopLossPercent(positionStopLoss);
		accountSetting.setFreezePercent(frozenStopLoss);
		accountSetting.setTerminatePercent(terminateStopLoss);
		accountSetting.setLiveTrading(true);
		System.out.printf("positionStopLoss:%s",positionStopLoss);
		return setting;
		
	}

}
