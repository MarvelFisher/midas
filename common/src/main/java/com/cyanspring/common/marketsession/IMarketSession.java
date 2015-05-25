package com.cyanspring.common.marketsession;
import com.cyanspring.common.staticdata.RefData;

import java.util.Date;
import java.util.Map;


public interface IMarketSession {
	public void init(Date date, RefData refData) throws Exception;
	public MarketSessionData getState(Date date, RefData refData) throws Exception;
    public String getTradeDate();
    public Map<String, MarketSession> getStateMap();
}
