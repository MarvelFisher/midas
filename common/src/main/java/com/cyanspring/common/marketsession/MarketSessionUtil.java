package com.cyanspring.common.marketsession;

import com.cyanspring.common.staticdata.RefData;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketSessionUtil {

    private Map<String, MarketSessionChecker> cMap;
	private Map<String, TradeDateManager> tMap;
	
	public MarketSessionUtil(Map<String, MarketSessionChecker> cMap, Map<String, TradeDateManager> tMap){
		this.cMap = cMap;
		this.tMap = tMap;
	}
	
	public MarketSessionData getCurrentMarketSessionType(RefData refData, Date date) throws Exception{
		MarketSessionChecker checker = cMap.get(refData.getStrategy());
		return checker.getState(date, refData);
	}
	
	public boolean isHoliday(String symbol, Date date){
		TradeDateManager checker = tMap.get(symbol);		
		return checker.isHoliday(date);
	}

    public Map<String, MarketSessionData> getMarketDatas(List<String> indexList, Date date) throws Exception {
        Map<String, MarketSessionData> dataMap = new HashMap<String, MarketSessionData>();
        for (Map.Entry<String, MarketSessionChecker> entry : cMap.entrySet()) {
            if (indexList == null)
                dataMap.put(entry.getKey(), entry.getValue().getState(date, null));
            else if (indexList.contains(entry.getKey()))
                dataMap.put(entry.getKey(), entry.getValue().getState(date, null));
        }
        return dataMap;
    }
}
