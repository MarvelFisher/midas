package com.cyanspring.server.livetrading.rule;

import java.util.Date;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountSettingType;
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
		double positionStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.POSITION_STOP_LOSS));
		double frozenStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS));
		double terminateStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS));
		double positionStopLossValue = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.COMPANY_STOP_LOSS_VALUE));
		double frozenStopLossValue = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS_VALUE));
		double terminateStopLossValue = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS_VALUE));
	
		setting.setStopLossPercent(positionStopLoss);
		setting.setFreezePercent(frozenStopLoss);
		setting.setTerminatePercent(terminateStopLoss);
		setting.setCompanySLValue(positionStopLossValue);
		setting.setFreezeValue(frozenStopLossValue);
		setting.setTerminateValue(terminateStopLossValue);
		
		if(newAccountSetting.fieldExists(AccountSettingType.LIVE_TRADING.value())){
			oldAccountSetting.setLiveTrading(newAccountSetting.isLiveTrading());
		}
		if(newAccountSetting.fieldExists(AccountSettingType.USER_LIVE_TRADING.value())){
			oldAccountSetting.setUserLiveTrading(newAccountSetting.isUserLiveTrading());
		}
		setting.setLiveTradingType(LiveTradingType.DEFAULT);
		setting.setLiveTradingSettedDate(TimeUtil.formatDate(TimeUtil.getOnlyDate(new Date()), dateFormat));
		return setting;
		
	}

}
