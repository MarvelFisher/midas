package com.cyanspring.server.livetrading.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.server.livetrading.LiveTradingException;
import com.cyanspring.server.livetrading.rule.IUserLiveTradingRule;
import com.cyanspring.server.livetrading.rule.LiveTradingType;

public class LiveTradingCheckHandler {
	
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingCheckHandler.class);	
	@Autowired
	private IRemoteEventManager eventManager;
	private Map <LiveTradingType,List> checkMap = new HashMap();
	
	public LiveTradingCheckHandler(Map <LiveTradingType,List> map) {
		checkMap = map;
	}
	
	public void showAllCheck() throws LiveTradingException{
		
		if(null != checkMap){
			Set<Entry<LiveTradingType, List>>entrySet = checkMap.entrySet();
			for(Entry <LiveTradingType,List>e:entrySet){				
				log.info("key:{} value:{}",e.getKey(),e.getValue());
				List <ILiveTradingChecker>rule =e.getValue();
				for(ILiveTradingChecker checker: rule){
					checker.check(null, null);
				}
			}
		}
		
	}
	
	

}
