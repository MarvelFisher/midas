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
		
		double positionStopLoss = parseDouble(paramsMap.get(LiveTradingFieldType.POSITION_STOP_LOSS));
		double frozenStopLoss = parseDouble(paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS));
		double terminateStopLoss = parseDouble(paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS));
		double positionStopLossValue = parseDouble(paramsMap.get(LiveTradingFieldType.COMPANY_STOP_LOSS_VALUE));
		double frozenStopLossValue = parseDouble(paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS_VALUE));
		double terminateStopLossValue = parseDouble(paramsMap.get(LiveTradingFieldType.TERMINATE_STOP_LOSS_VALUE));
		
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

	private double parseDouble(Object obj){
		if( obj instanceof String){
			 Double.parseDouble((String)obj);
		}else if(obj instanceof Double){
			 return (Double)obj;
		}
		return 0;
	}
}
