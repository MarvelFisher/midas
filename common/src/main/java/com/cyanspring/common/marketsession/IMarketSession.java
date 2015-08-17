package com.cyanspring.common.marketsession;
import com.cyanspring.common.staticdata.RefData;

import java.util.Date;
import java.util.Map;


public interface IMarketSession {
	public void init(Date date, RefData refData) throws Exception;
	public MarketSessionData getMarketSessionState(Date date, RefData refData) throws Exception;
    public MarketSessionData searchState(Date date, RefData refData) throws Exception;
    public String getIndex();
    public String getTradeDate();
    public ITradeDate getTradeDateManager();
    public Map<String, MarketSession> getStateMap();
}
