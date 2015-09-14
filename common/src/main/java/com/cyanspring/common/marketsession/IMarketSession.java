package com.cyanspring.common.marketsession;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.staticdata.RefData;


public interface IMarketSession {
	public void init(Date date, RefData refData) throws Exception;
	public MarketSessionData getState(Date date, RefData refData) throws Exception;
    public MarketSessionData searchState(Date date, RefData refData) throws Exception;
    public MarketSession getMarketSession(Date date, RefData refData) throws Exception;
    public String getIndex();
    public String getTradeDate();
    public ITradeDate getTradeDateManager();
    public Map<String, MarketSession> getStateMap();
}
