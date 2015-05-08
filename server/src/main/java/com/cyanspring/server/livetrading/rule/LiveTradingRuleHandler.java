package com.cyanspring.server.livetrading.rule;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.account.PmChangeAccountSettingEvent;
import com.cyanspring.common.event.livetrading.LiveTradingAccountSettingReplyEvent;
import com.cyanspring.common.event.livetrading.LiveTradingAccountSettingRequestEvent;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.livetrading.LiveTradingException;
import com.cyanspring.server.persistence.PersistenceManager;

public class LiveTradingRuleHandler implements IPlugin{
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingRuleHandler.class);	
	
	@Autowired
	private IRemoteEventManager eventManager;
	
    @Autowired
    AccountKeeper accountKeeper;
	
	private Map <LiveTradingType,IUserLiveTradingRule> ruleMap = new HashMap<LiveTradingType,IUserLiveTradingRule>();
	
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {		
			subscribeToEvent(LiveTradingAccountSettingRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public LiveTradingRuleHandler(Map <LiveTradingType,IUserLiveTradingRule> map) {
		ruleMap = map;
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
	
	public void processLiveTradingAccountSettingRequestEvent(LiveTradingAccountSettingRequestEvent event){
		//get event and get LiveTradingType and AccountContent
		boolean isSettingOk = false;
		try {
			
			Account account = event.getAccount();
			AccountSetting setting = event.getAccountSetting();
			LiveTradingType type = event.getLiveTradingType();
			if(null != setting){
				type = LiveTradingType.CUSTOM;
			}
			setting = accountKeeper.getAccountSetting(account.getId());
			setTradingRule(type,account,setting);
			isSettingOk = true;
			
		} catch (LiveTradingException e) {
			log.error(e.getMessage(),e);
			isSettingOk = false;
		} catch (AccountException e) {
			log.error(e.getMessage(),e);
			isSettingOk = false;
		}
		
		LiveTradingAccountSettingReplyEvent replyEvent = new LiveTradingAccountSettingReplyEvent(event.getKey(),event.getReceiver(),isSettingOk);
		try {
			eventManager.sendRemoteEvent(replyEvent);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		
	}
	
	private void sendAccountSettingChangeEvent(AccountSetting accountSetting){
        PmChangeAccountSettingEvent pmEvent = new PmChangeAccountSettingEvent(PersistenceManager.ID,
                null, accountSetting);
        eventManager.sendEvent(pmEvent);
	}

	@Override
	public void init() throws Exception {
		
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null){
			eventProcessor.getThread().setName("LiveTradingRuleHandler");
		}
		
	}

	@Override
	public void uninit() {
		eventProcessor.uninit();
		eventManager.uninit();		
	}

}
