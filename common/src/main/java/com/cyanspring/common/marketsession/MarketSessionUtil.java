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

    private Map<String, IMarketSession> cMap;
    private Map<String, ITradeDate> tMap;
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
    			ret.put(pair.index, pair.session.getState(date, refData));
    	}
    	return ret;
    }
    
    private SessionPair getSession(RefData refData) {
    	for (Entry<String, IMarketSession> entry : sessionMap.entrySet()) {
    		String key = entry.getKey();
    		if(compareIndex(key, refData.getSymbol()) ||
    				compareIndex(key, refData.getExchange()) ||
    				compareIndex(key, refData.getSpotENName()))
    			return getPair(refData, entry.getValue());
    	}
    	return null;
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

    public MarketSessionUtil(Map<String, IMarketSession> cMap, Map<String, ITradeDate> tMap) {
        this.cMap = cMap;
        this.tMap = tMap;
    }

    public MarketSessionData getCurrentMarketSessionType(RefData refData, Date date, boolean searchBySymbol) throws Exception {
        IMarketSession checker = null;
        checker = cMap.get(refData.getStrategy());
        return searchBySymbol ? checker.getState(date, refData) : checker.getState(date, null);
    }

    public boolean isHoliday(String symbol, Date date) {
        ITradeDate checker = tMap.get(symbol);
        return checker.isHoliday(date);
    }

    public Map<String, MarketSessionData> getSessionDataBySymbol(List<RefData> indexList, Date date) throws Exception {
        Map<String, MarketSessionData> dataMap = new HashMap<String, MarketSessionData>();
        for (RefData refData : indexList) {
            IMarketSession checker = cMap.get(refData.getStrategy());
            if (checker == null)
                continue;
            dataMap.put(refData.getSymbol(), checker.getState(date, refData));
        }
        return dataMap;
    }

    public Map<String, MarketSessionData> getSessionDataByStrategy(List<String> indexList, Date date) throws Exception {
        Map<String, MarketSessionData> dataMap = new HashMap<String, MarketSessionData>();
        for (Map.Entry<String, IMarketSession> entry : cMap.entrySet()) {
            if (indexList == null || indexList.size() == 0)
                dataMap.put(entry.getKey(), entry.getValue().getState(date, null));
            else if (indexList.contains(entry.getKey()))
                dataMap.put(entry.getKey(), entry.getValue().getState(date, null));
        }
        return dataMap;
    }

    public Map<String, Map<String, MarketSession>> getAll() {
        Map<String, Map<String, MarketSession>> map = new HashMap<String, Map<String, MarketSession>>();
        for (Map.Entry<String, IMarketSession> entry : cMap.entrySet()){
            map.put(entry.getKey(), entry.getValue().getStateMap());
        }
        return map;
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
