package com.cyanspring.common.marketsession;

import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataUtil;
import com.cyanspring.common.staticdata.fu.IndexSessionType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MarketSessionUtil {
    private Map<String, IMarketSession> sessionMap;
    
    public MarketSessionUtil(List<IMarketSession> sessionList) {
    	sessionMap = new HashMap<>();
    	for(IMarketSession session : sessionList){
    		sessionMap.put(session.getIndex(), session);
    	}
    }
    
    public Map<String, MarketSessionData> getMarketSession(List<RefData> indexList, Date date) throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for(RefData refData : indexList) {
    		SessionPair pair = getSession(refData); 
    		if (pair != null)
    			ret.put(pair.index, pair.session.getMarketSessionState(date, refData));
    	}
    	return ret;
    }
    
    public Map<String, MarketSessionData> getMarketSession(List<RefData> indexList, Map<String, Date> dateMap) throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for(RefData refData : indexList) {
    		SessionPair pair = getSession(refData); 
    		ret.put(pair.index, pair.session.getMarketSessionState(dateMap.get(refData.getSymbol()), refData));
    	}
    	return ret;
    }
    
    private SessionPair getSession(RefData refData) throws Exception {
    	for (Entry<String, IMarketSession> entry : sessionMap.entrySet()) {
    		String key = entry.getKey();
    		if(compareIndex(key, refData.getSymbol()) ||
    				compareIndex(key, refData.getExchange()) ||
    				compareIndex(key, refData.getSpotENName()))
    			return getPair(refData, entry.getValue());
    	}
    	throw new Exception("No session data found");
    }
    
    private boolean compareIndex(String c1, String c2){
    	String comp1 = RefDataUtil.getOnlyChars(c1);
    	String comp2 = RefDataUtil.getOnlyChars(c2);
    	return comp2.toLowerCase().equals(comp1.toLowerCase());
    }
    
    private SessionPair getPair(RefData refData, IMarketSession session) {
    	String sessionIndex = refData.getIndexSessionType();
    	if (sessionIndex.equals(IndexSessionType.SETTLEMENT.toString()))
    		return new SessionPair(refData.getSymbol(), session);
    	else if (sessionIndex.equals(IndexSessionType.EXCHANGE.toString()))
    		return new SessionPair(refData.getExchange(), session);
    	else if (sessionIndex.equals(IndexSessionType.SPOT.toString()))
    		return new SessionPair(refData.getSpotENName(), session);
    	return null;
    }

    public boolean isHoliday(String symbol, Date date) throws Exception{
    	for (Entry<String, IMarketSession> entry : sessionMap.entrySet()) {
    		if (compareIndex(entry.getKey(), symbol)) {
    			ITradeDate checker = entry.getValue().getTradeDateManager();
    			return checker.isHoliday(date);
    		}
    	}
    	throw new Exception("Symbol: " + symbol + " not found in the map");
    }

    private class SessionPair {
    	private String index;
    	private IMarketSession session;
    	public SessionPair(String index, IMarketSession session){
    		this.index = index;
    		this.session = session;
    	}
    }
}
