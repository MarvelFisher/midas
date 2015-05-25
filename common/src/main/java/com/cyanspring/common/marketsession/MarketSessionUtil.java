package com.cyanspring.common.marketsession;

import com.cyanspring.common.staticdata.RefData;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketSessionUtil {

    private Map<String, IMarketSession> cMap;
    private Map<String, ITradeDate> tMap;

    public MarketSessionUtil(Map<String, IMarketSession> cMap, Map<String, ITradeDate> tMap) {
        this.cMap = cMap;
        this.tMap = tMap;
    }

    public MarketSessionData getCurrentMarketSessionType(RefData refData, Date date, boolean searchBySymbol) throws Exception {
        IMarketSession checker = null;
        if (refData.getStrategy() != null)
            checker = cMap.get(refData.getStrategy());
        if (checker == null)
            checker = cMap.get(refData.getExchange());
        return searchBySymbol ? checker.getState(date, refData) : checker.getState(date, null);
    }

    public boolean isHoliday(String symbol, Date date) {
        ITradeDate checker = tMap.get(symbol);
        return checker.isHoliday(date);
    }

    public Map<String, MarketSessionData> getSessionDataBySymbol(List<RefData> indexList, Date date) throws Exception {
        Map<String, MarketSessionData> dataMap = new HashMap<String, MarketSessionData>();
        for (RefData refData : indexList) {
            IMarketSession checker = null;
            if (refData.getStrategy() != null)
                checker = cMap.get(refData.getStrategy());
            if (checker == null)
                checker = cMap.get(refData.getExchange());
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
}
