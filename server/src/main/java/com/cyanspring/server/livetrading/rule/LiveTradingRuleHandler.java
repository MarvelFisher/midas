package com.cyanspring.server.livetrading.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.server.livetrading.LiveTradingException;
import com.cyanspring.server.persistence.PersistenceManager;

public class LiveTradingRuleHandler {
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingRuleHandler.class);	
	@Autowired
	private IRemoteEventManager eventManager;
	private Map <LiveTradingType,IUserLiveTradingRule> ruleMap = new HashMap();
	
	public LiveTradingRuleHandler(Map <LiveTradingType,IUserLiveTradingRule> map) {
		ruleMap = map;
	}
	
	public void showAllRule() throws LiveTradingException{
		
		if(null != ruleMap){
			Set<Entry<LiveTradingType, IUserLiveTradingRule>>entrySet = ruleMap.entrySet();
			for(Entry <LiveTradingType,IUserLiveTradingRule>e:entrySet){				
				log.info("key:{} value:{}",e.getKey(),e.getValue());
				IUserLiveTradingRule rule =e.getValue();
				//rule.setRule(null,null);
			}
		}
		
	}
	
	public void setTradingRule(LiveTradingType type,Account account,AccountSetting accountSetting)throws LiveTradingException{
		
		log.info("{} setTradingRule:{}",accountSetting.getId(),type);
		if(null == ruleMap || 0 == ruleMap.size()){
			throw new LiveTradingException("No trading rule in map");
		}
		if(!ruleMap.containsKey(type)){
			throw new LiveTradingException("This rule doen't exist in map:"+type);
		}
		
		IUserLiveTradingRule rule = ruleMap.get(type);		
		accountSetting = rule.setRule(account,accountSetting);
		sendAccountSettingChangeEvent(accountSetting);
		
	}
	
	private void sendAccountSettingChangeEvent(AccountSetting accountSetting){
        PmChangeAccountSettingEvent pmEvent = new PmChangeAccountSettingEvent(PersistenceManager.ID,
                null, accountSetting);
        eventManager.sendEvent(pmEvent);
	}

}
