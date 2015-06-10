package com.cyanspring.server.livetrading.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.event.IRemoteEventManager;

public class LiveTradingCheckHandler {
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingCheckHandler.class);	
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	private Map <LiveTradingType,List<ILiveTradingChecker>> checkMap = new HashMap<LiveTradingType,List<ILiveTradingChecker>>();
	
	public LiveTradingCheckHandler(Map <LiveTradingType,List<ILiveTradingChecker>> map) {
		checkMap = map;
	}
	
	public boolean startCheckChain(Account account, AccountSetting accountSetting){
		
		if(!accountSetting.isUserLiveTrading()){
			return false;
		}
		if( null == checkMap ){
			return false;
		}
		if(!checkMap.containsKey(accountSetting.getLiveTradingType())){
			log.warn("Live trading : can't find this checkchain - {}",accountSetting.getLiveTradingType());
			return false;
		}
		
		List <ILiveTradingChecker>checkList = checkMap.get(accountSetting.getLiveTradingType());
		
		for(ILiveTradingChecker checker: checkList){
			boolean needNextCheck = checker.check(account, accountSetting);
			if(!needNextCheck){
				return true;//find need stop loss
			}
		}
		return false;
	
	}

}
