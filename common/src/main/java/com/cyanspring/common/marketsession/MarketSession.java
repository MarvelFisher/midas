package com.cyanspring.common.marketsession;
import java.util.List;


public class MarketSession {
	private List<MarketSessionData> sessionDatas;
	
	public MarketSession(List<MarketSessionData> sessionDatas){
		this.sessionDatas = sessionDatas;
	}

	public List<MarketSessionData> getSessionDatas() {
		return sessionDatas;
	}

	public void setSessionDatas(List<MarketSessionData> sessionDatas) {
		this.sessionDatas = sessionDatas;
	}
}
