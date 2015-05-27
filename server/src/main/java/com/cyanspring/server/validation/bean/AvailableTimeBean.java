package com.cyanspring.server.validation.bean;

import java.text.SimpleDateFormat;

import com.cyanspring.common.marketsession.MarketSessionType;

public class AvailableTimeBean {
	
	private MarketSessionType marketSession;
	private String startTime;
	private String endTime;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			
	public AvailableTimeBean() {
		
	}
	
	public MarketSessionType getMarketSession() {
		return marketSession;
	}

	public void setMarketSession(MarketSessionType marketSession) {
		this.marketSession = marketSession;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}	
	
}
