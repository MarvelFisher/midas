package com.cyanspring.common.marketsession;
import java.util.List;


public class MarketSession {

	private boolean crossDay;
	private String openingTime;
	private List<MarketSessionData> sessionDatas;

	public MarketSession(List<MarketSessionData> sessionDatas) {
		this.sessionDatas = sessionDatas;
	}

	public MarketSession(boolean crossDay, String openingTime, List<MarketSessionData> sessionDatas) {
		this.crossDay = crossDay;
		this.openingTime = openingTime;
		this.sessionDatas = sessionDatas;
	}

	public String getOpeningTime() {
		return openingTime;
	}

	public boolean isCrossDay() {
		return crossDay;
	}

	public void setCrossDay(boolean crossDay) {
		this.crossDay = crossDay;
	}

	public void setOpeningTime(String openingTime) {
		this.openingTime = openingTime;
	}

	public List<MarketSessionData> getSessionDatas() {
		return sessionDatas;
	}

	public void setSessionDatas(List<MarketSessionData> sessionDatas) {
		this.sessionDatas = sessionDatas;
	}

}
