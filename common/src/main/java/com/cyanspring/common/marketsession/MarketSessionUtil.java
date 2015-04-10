package com.cyanspring.common.marketsession;

import java.util.Date;
import java.util.Map;

public class MarketSessionUtil {
	private Map<String, MarketSessionChecker> cMap;
	private Map<String, TradeDateManager> tMap;
	
	public MarketSessionUtil(Map<String, MarketSessionChecker> cMap, Map<String, TradeDateManager> tMap){
		this.cMap = cMap;
		this.tMap = tMap;
	}
	
	public MarketSessionData getCurrentMarketSessionType(String symbol, Date date) throws Exception{
		MarketSessionChecker checker = cMap.get(symbol);
		return checker.getState(date);
	}
	
	public boolean isHoliday(String symbol, Date date){
		TradeDateManager checker = tMap.get(symbol);		
		return checker.isHoliday(date);
	}
}
