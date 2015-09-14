package com.cyanspring.common.marketsession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IPlugin;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataService;
import com.cyanspring.common.staticdata.RefDataUtil;
import com.cyanspring.common.staticdata.fu.IndexSessionType;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.OrderValidationException;

public class MarketSessionUtil implements IPlugin{
	private static final Logger log = LoggerFactory.getLogger(MarketSessionUtil.class);
    private Map<String, IMarketSession> sessionMap;
    
    @Autowired
    private RefDataService refDataManager; 
    
    public MarketSessionUtil(List<IMarketSession> sessionList) {
    	sessionMap = new HashMap<>();
    	for(IMarketSession session : sessionList){
    		sessionMap.put(session.getIndex(), session);
    	}
    }
    
    public Map<String, MarketSessionData> getMarketSession() throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	Date now = Clock.getInstance().now();
    	for (Entry<String, IMarketSession> entry : sessionMap.entrySet()) {
    		ret.put(entry.getKey(), entry.getValue().getState(now, null));
    	}
    	return ret;
    }
    
    public MarketSessionData getMarketSession(RefData refData, Date date) throws Exception {
    	SessionPair pair = getSession(refData);
    	if (pair == null) {
    		log.warn("Can't find market session, symbol: {}", refData.getSymbol());
    		return null;
    	}
    	return pair.session.getState(date, refData);
    }
    
    public MarketSessionData getCurrentMarketSession(String symbol) throws Exception{
    	RefData refData = refDataManager.getRefData(symbol);
    	if(null == refData)
    		return null;
    	String index = searchIndex(refData);
    	if(!StringUtils.hasText(index))
    		return null;
    	
    	MarketSessionData data = this.getMarketSession().get(index);
    	return data;
    }
    
    public List<String> getSymbolListByIndexSessionKey(String key){
    	
    	List <String>symbolList = new ArrayList<String>();
    	
    	if( null == refDataManager)
    		return symbolList;
    	
    	List<RefData> refs = refDataManager.getRefDataList();
    	
    	if(null == refs || refs.isEmpty())
    		return symbolList;
    	
    	boolean isCategory = false;
    	boolean isExchange = false;
    	
    	Iterator <RefData>refDataIte = refs.iterator();
    	while(refDataIte.hasNext()){
    		RefData ref = refDataIte.next();
    		String category = ref.getCategory();
    		String symbol = ref.getSymbol();
    		String exchange = ref.getExchange();
    		if(!StringUtils.hasText(category) 
    				|| !StringUtils.hasText(symbol)
    				|| !StringUtils.hasText(exchange)){
    			continue;
    		}
    		
    		if(symbol.equals(key)){
    			symbolList.add(symbol);
    			break;
    		}else if(category.equals(key)){
    			isCategory = true;
    			break;
    		}else if( exchange.equals(key)){
    			isExchange = true;
    			break;
    		}
    	}
    	    	
    	if(isCategory){
    		for(RefData ref : refs){
    			if(ref.getCategory().equals(key)
    					&& StringUtils.hasText(ref.getIndexSessionType())
    					&& !ref.getIndexSessionType().equals(IndexSessionType.SETTLEMENT)){
    				symbolList.add(ref.getSymbol());
    			}
    		}
    	}
    	
    	if(isExchange){
    		for(RefData ref : refs){
    			if(ref.getExchange().equals(key)
    					&& StringUtils.hasText(ref.getIndexSessionType())
    					&& !ref.getIndexSessionType().equals(IndexSessionType.SETTLEMENT)){
    				symbolList.add(ref.getSymbol());
    			}
    		}
    	}
    	
    	return symbolList;
    }
    
    public Map<String, MarketSessionData> getMarketSession(List<RefData> indexList, Date date) throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for(RefData refData : indexList) {
    		SessionPair pair = getSession(refData); 
    		if (pair != null)
    			ret.put(pair.index, pair.session.getState(date, refData));
    		else
    			log.warn("Can't find market session, symbol: {}", refData.getSymbol());
    	}
    	return ret;
    }
    
    public Map<String, MarketSessionData> getMarketSession(List<RefData> indexList, Map<String, Date> dateMap) throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for(RefData refData : indexList) {
    		SessionPair pair = getSession(refData);
    		if (pair != null)
    			ret.put(pair.index, pair.session.getState(dateMap.get(refData.getSymbol()), refData));
    		else
    			log.warn("Can't find market session, symbol: {}", refData.getSymbol());
    	}
    	return ret;
    }
    
    public MarketSession getMarketSessions(Date date, RefData refData) throws Exception{
    	String index = searchIndex(refData);
    	if (!StringUtils.hasText(index)) 
    		return null;
    	IMarketSession session = sessionMap.get(index);
    	if (session == null)
    		return null;
    	return session.getMarketSession(date, refData);
    }
    
    public ITradeDate getTradeDateManager(String index){
    	if(sessionMap.containsKey(index) && null != sessionMap.get(index)){
    		return sessionMap.get(index).getTradeDateManager();
    	}
    	return null;
    }
    
    public ITradeDate getTradeDateManagerBySymbol(String symbol){
    	RefData refData = getRefData(symbol);
    	String index = searchIndex(refData);
    	if (!StringUtils.hasText(index))
    		return null;
    	return getTradeDateManager(index);
    }
    
    public RefData getRefData(String symbol){
    	RefData refData = refDataManager.getRefData(symbol);
    	if(null == refData)
    		return null;
    	
    	return refData;
    }
    
    private SessionPair getSession(RefData refData) throws Exception{
    	String index = searchIndex(refData);
    	for (Entry<String, IMarketSession> entry : sessionMap.entrySet()) {
    		if(entry.getKey().equals(index))
    			return getPair(refData, entry.getValue());
    	}
    	return null;
    }
    
    private boolean compareIndex(String c1, String c2){
    	if(c1 == null || c2 == null)
    		return false;
    	String comp1 = RefDataUtil.getOnlyChars(c1);
    	String comp2 = RefDataUtil.getOnlyChars(c2);
    	return comp2.toLowerCase().equals(comp1.toLowerCase());
    }
    
    private SessionPair getPair(RefData refData, IMarketSession session) throws Exception {
    	String sessionIndex = refData.getIndexSessionType();
    	if (sessionIndex == null)
    		throw new Exception("Null indexSessionType, symbol: " + refData.getSymbol());
    	if (sessionIndex.equals(IndexSessionType.SETTLEMENT.toString()))
    		return new SessionPair(refData.getSymbol(), session);
    	else if (sessionIndex.equals(IndexSessionType.SPOT.toString()))
    		return new SessionPair(refData.getCategory(), session);
    	else if (sessionIndex.equals(IndexSessionType.EXCHANGE.toString()))
    		return new SessionPair(refData.getExchange(), session);
    	return null;
    }
    
    private String searchIndex(RefData refData) {
    	String index = null;
    	index = RefDataUtil.getSearchIndex(refData);
    	if (index == null) {
    		if (StringUtils.hasText(refData.getExchange()))
    			index = refData.getExchange();
    		else if (StringUtils.hasText(refData.getCategory()))
    			index = refData.getCategory();
    		else if (StringUtils.hasText(refData.getSymbol()))
    			index = RefDataUtil.getOnlyChars(refData.getSymbol());
    	}
    	return index;
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

	@Override
	public void init() throws Exception {
		Date date = Clock.getInstance().now();
		for (Entry<String, IMarketSession> e : sessionMap.entrySet()) {
			e.getValue().init(date, null);
		}
	}

	@Override
	public void uninit() {
		
	}
}
