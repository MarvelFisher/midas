package com.cyanspring.common.marketsession;
import java.util.Date;


public interface IMarketSession {
	public void init(Date date) throws Exception;
	public MarketSessionData getState(Date date) throws Exception;
}
