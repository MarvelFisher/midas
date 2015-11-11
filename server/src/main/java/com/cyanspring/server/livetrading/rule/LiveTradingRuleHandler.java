package com.cyanspring.server.livetrading.rule;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.AccountSettingType;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.livetrading.TradingUtil;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.PriceUtils;

public class LiveTradingRuleHandler{
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingRuleHandler.class);	
	
	private Map <LiveTradingType,IUserLiveTradingRule> ruleMap = new HashMap<LiveTradingType,IUserLiveTradingRule>();
	
	public LiveTradingRuleHandler(Map <LiveTradingType,IUserLiveTradingRule> map) {
		ruleMap = map;
	}
	
	public AccountSetting setTradingRule(AccountSetting oldAccountSetting,AccountSetting newAccountSetting,Account account)throws AccountException{
		LiveTradingType type = newAccountSetting.getLiveTradingType();
		if(null == type){
			type = oldAccountSetting.getLiveTradingType();
		}
		
		log.info("{} setLiveTradingRule:{}",newAccountSetting.getId(),type);
		if(null == ruleMap || 0 == ruleMap.size()){
			throw new AccountException("Live Trading : No trading rule in map",ErrorMessage.LIVE_TRADING_NO_RULE_IN_MAP);
		}
		if(!ruleMap.containsKey(type)){
			throw new AccountException("Live Trading : This rule doesn't exist in map:"+type,ErrorMessage.LIVE_TRADING_NO_RULE_IN_MAP);
		}
		
		IUserLiveTradingRule rule = ruleMap.get(type);		
		oldAccountSetting = rule.setRule(oldAccountSetting,newAccountSetting);
		
		if(null != account)
			oldAccountSetting = checkLiveTradingStopLossValue(oldAccountSetting,account);
		
		return oldAccountSetting;	
	}
	private AccountSetting checkLiveTradingStopLossValue(AccountSetting oldSetting,Account account)throws AccountException{
		
		double comDailyStopLoss = TradingUtil.getMinValue(account.getStartAccountValue()*oldSetting.getFreezePercent()
				, oldSetting.getFreezeValue());
		
		double comPositionStopLoss = TradingUtil.getMinValue(account.getStartAccountValue()*oldSetting.getStopLossPercent()
				, oldSetting.getCompanySLValue());
		
		if(!PriceUtils.isZero(comDailyStopLoss) 
				&& PriceUtils.GreaterThan(oldSetting.getDailyStopLoss(), comDailyStopLoss)){
			
			oldSetting.setDailyStopLoss(comDailyStopLoss);
		}
		
		if(!PriceUtils.isZero(comPositionStopLoss) 
				&& PriceUtils.GreaterThan(oldSetting.getStopLossValue(), comPositionStopLoss)){
			
			oldSetting.setStopLossValue(comPositionStopLoss);
		}
		return oldSetting;
	}
	
	public boolean isNeedSetting(AccountSetting oldAccountSetting,AccountSetting newAccountSetting) {

		if(newAccountSetting.fieldExists(AccountSettingType.LIVE_TRADING.value())
				&& oldAccountSetting.isLiveTrading() != newAccountSetting.isLiveTrading()){
			return true;
		}
		if(newAccountSetting.fieldExists(AccountSettingType.USER_LIVE_TRADING.value())
				&& oldAccountSetting.isUserLiveTrading() != newAccountSetting.isUserLiveTrading()){
			return true;
		}
		if(newAccountSetting.fieldExists(AccountSettingType.LIVE_TRADING_TYPE.value())
				&& !oldAccountSetting.getLiveTradingType().equals(newAccountSetting.getLiveTradingType())){
			return true;
		}
		
		return false;
	}

}
