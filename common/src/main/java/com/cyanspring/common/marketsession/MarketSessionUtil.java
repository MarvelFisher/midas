package com.cyanspring.common.marketsession;

import java.util.ArrayList;
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
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataService;
import com.cyanspring.common.staticdata.RefDataUtil;

public class MarketSessionUtil implements IPlugin {
	private static final Logger log = LoggerFactory.getLogger(MarketSessionUtil.class);
    private Map<String, IMarketSessionChecker> sessionMap;

    @Autowired
    private RefDataService refDataManager;

    public MarketSessionUtil(List<IMarketSessionChecker> sessionList) {
    	sessionMap = new HashMap<>();
    	for (IMarketSessionChecker session : sessionList) {
    		sessionMap.put(session.getIndex(), session);
    	}
    }

    public Map<String, MarketSessionData> getMarketSession() throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for (Entry<String, IMarketSessionChecker> entry : sessionMap.entrySet()) {
    		ret.put(entry.getKey(), entry.getValue().getState(null));
    	}
    	return ret;
    }

    public MarketSessionData getMarketSession(RefData refData) throws Exception {
    	SessionPair pair = getSession(refData);
    	if (pair == null) {
    		log.warn("Can't find market session, symbol: {}", refData.getSymbol());
    		return null;
    	}
    	return pair.session.getState(refData);
    }

    public MarketSessionData getCurrentMarketSession(String symbol) throws Exception {
    	RefData refData = refDataManager.getRefData(symbol);
    	if (refData == null) {
			return null;
		}

    	MarketSessionData data = getMarketSession(refData);
    	return data;
    }

    public List<String> getSymbolListByIndexSessionKey(String key) {

    	List <String>symbolList = new ArrayList<String>();

    	if (refDataManager == null) {
			return symbolList;
		}

    	List<RefData> refs = refDataManager.getRefDataList();

    	if (refs == null || refs.isEmpty()) {
			return symbolList;
		}

    	boolean isCategory = false;
    	boolean isExchange = false;

    	Iterator <RefData>refDataIte = refs.iterator();
    	while (refDataIte.hasNext()) {
    		RefData ref = refDataIte.next();
    		String category = ref.getCategory();
    		String symbol = ref.getSymbol();
    		String exchange = ref.getExchange();
    		if (!StringUtils.hasText(category)
    				|| !StringUtils.hasText(symbol)
    				|| !StringUtils.hasText(exchange)) {
    			continue;
    		}

    		if (symbol.equals(key)) {
    			symbolList.add(symbol);
    			break;
    		} else if (category.equals(key)) {
    			isCategory = true;
    			break;
    		} else if (exchange.equals(key)) {
    			isExchange = true;
    			break;
    		}
    	}

    	if (isCategory) {
    		for (RefData ref : refs) {
    			if (ref.getCategory().equals(key)
    					&& StringUtils.hasText(ref.getIndexSessionType())
    					&& !ref.getIndexSessionType().equals(IndexSessionType.SETTLEMENT)) {
    				symbolList.add(ref.getSymbol());
    			}
    		}
    	}

    	if (isExchange) {
    		for (RefData ref : refs) {
    			if (ref.getExchange().equals(key)
    					&& StringUtils.hasText(ref.getIndexSessionType())
    					&& !ref.getIndexSessionType().equals(IndexSessionType.SETTLEMENT)) {
    				symbolList.add(ref.getSymbol());
    			}
    		}
    	}

    	return symbolList;
    }

    public Map<String, MarketSessionData> getMarketSession(List<RefData> indexList, Date date) throws Exception {
    	Map<String, MarketSessionData> ret = new HashMap<>();
    	for (RefData refData : indexList) {
    		SessionPair pair = getSession(refData);
    		if (pair != null) {
				ret.put(pair.index, pair.session.getState(refData));
			} else {
				log.warn("Can't find market session, symbol: {}", refData.getSymbol());
			}
    	}
    	return ret;
    }

    public MarketSession getMarketSessions(RefData refData, String date) throws Exception {
    	String index = searchIndex(refData);
    	if (!StringUtils.hasText(index)) {
			return null;
		}
    	IMarketSessionChecker session = sessionMap.get(index);
    	if (session == null) {
			return null;
		}
    	return session.getMarketSession(refData, date);
    }

    public ITradeDate getTradeDateManager(String index) {
    	if (sessionMap.containsKey(index) && null != sessionMap.get(index)) {
    		return sessionMap.get(index).getTradeDateManager();
    	}
    	return null;
    }

    public ITradeDate getTradeDateManagerBySymbol(String symbol) {
    	RefData refData = getRefData(symbol);
    	String index = searchIndex(refData);
    	if (!StringUtils.hasText(index)) {
			return null;
		}
    	return getTradeDateManager(index);
    }

    public RefData getRefData(String symbol) {
    	RefData refData = refDataManager.getRefData(symbol);
    	if (refData == null) {
			return null;
		}

    	return refData;
    }

    private SessionPair getSession(RefData refData) throws Exception {
    	String index = searchIndex(refData);
    	IMarketSessionChecker marketSession = sessionMap.get(index);
    	if (marketSession != null) {
			return getPair(refData, marketSession);
		}
    	return null;
    }

    private SessionPair getPair(RefData refData, IMarketSessionChecker session) throws Exception {
    	String sessionIndex = refData.getIndexSessionType();
    	if (sessionIndex == null) {
			throw new Exception("Null indexSessionType, symbol: " + refData.getSymbol());
		}
    	if (sessionIndex.equals(IndexSessionType.SETTLEMENT.toString())) {
			return new SessionPair(refData.getSymbol(), session);
		} else if (sessionIndex.equals(IndexSessionType.SPOT.toString())) {
			return new SessionPair(refData.getCategory(), session);
		} else if (sessionIndex.equals(IndexSessionType.EXCHANGE.toString())) {
			return new SessionPair(refData.getExchange(), session);
		}
    	return null;
    }

    private String searchIndex(RefData refData) {
    	String index = null;
    	index = RefDataUtil.getSearchIndex(refData);
    	if (index != null && index.equals(refData.getSymbol())) {
    		if (checkExist(index)) {
    			return index;
    		}
    	}

    	if (StringUtils.hasText(refData.getCategory())) {
			index = refData.getCategory();
		}
    	if (checkExist(index)) {
			return index;
		}
    	if (StringUtils.hasText(refData.getExchange())) {
			index = refData.getExchange();
		}
    	if (checkExist(index)) {
			return index;
		}
    	if (StringUtils.hasText(refData.getSymbol())) {
			index = refData.getSymbol();
		}
    	if (checkExist(index)) {
			return index;
		}
    	return index;
    }

    private boolean checkExist(String index) {
    	return sessionMap.get(index) != null ? true : false;
    }

    public boolean isHoliday(RefData refData, Date date) throws Exception {
    	for (Entry<String, IMarketSessionChecker> entry : sessionMap.entrySet()) {
    		String index = entry.getKey();
    		if (index.equals(refData.getSymbol()) || index.equals(refData.getCategory()) ||
    				index.equals(refData.getExchange())) {
    			ITradeDate checker = entry.getValue().getTradeDateManager();
    			return checker.isHoliday(date);
    		}
    	}
    	throw new Exception("Symbol: " + refData.getSymbol() + " not found in the map");
    }

    private class SessionPair {
    	private String index;
    	private IMarketSessionChecker session;
    	public SessionPair(String index, IMarketSessionChecker session) {
    		this.index = index;
    		this.session = session;
    	}
    }

	@Override
	public void init() throws Exception {
		Date date = Clock.getInstance().now();
		for (Entry<String, IMarketSessionChecker> e : sessionMap.entrySet()) {
			e.getValue().init(date, null);
		}
	}

	@Override
	public void uninit() {

	}
}
