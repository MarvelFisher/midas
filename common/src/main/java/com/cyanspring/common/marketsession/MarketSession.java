package com.cyanspring.common.marketsession;
import java.util.List;


public class MarketSession {

	private String openingTime;
	private List<MarketSessionData> sessionDatas;

	public String getOpeningTime() {
		return openingTime;
	}

	public void setOpeningTime(String openingTime) {
		this.openingTime = openingTime;
	}

	public MarketSession(List<MarketSessionData> sessionDatas){
		this.sessionDatas = sessionDatas;
	}

	public MarketSession(String openingTime, List<MarketSessionData> sessionDatas){
		this.openingTime = openingTime;
		this.sessionDatas = sessionDatas;
	}

	public List<MarketSessionData> getSessionDatas() {
		return sessionDatas;
	}

	public void setSessionDatas(List<MarketSessionData> sessionDatas) {
		this.sessionDatas = sessionDatas;
	}

}
