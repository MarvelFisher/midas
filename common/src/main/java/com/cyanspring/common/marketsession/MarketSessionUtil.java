package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import com.cyanspring.common.event.marketsession.MarketSessionEvent;

public class MarketSessionUtil {
	private Map<String, MarketSessionState> map;
	
	public MarketSessionUtil(Map<String, MarketSessionState> map){
		this.map = map;
	}
	
	public MarketSessionType getCurrentMarketSessionType(String market, Date date){
		MarketSessionState state = map.get(market);
		return state.getCurrentMarketSession(date);
	}
	
	public MarketSessionEvent getCurrentMarketSessionEvent(String market, Date date) throws ParseException{
		MarketSessionState state = map.get(market);
		return state.getCurrentMarketSessionEvent(date);
	}
}
