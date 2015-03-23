package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.Clock;
import com.cyanspring.common.util.TimeUtil;

public class MarketSessionData {
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private MarketSessionType sessionType;
	private String start;
	private String end;

	public MarketSessionData(MarketSessionType sessionType, String start,
			String end) {
		this.sessionType = sessionType;
		this.start = start;
		this.end = end;
	}
	
	public MarketSessionType getSessionType() {
		return sessionType;
	}
	public void setSessionType(MarketSessionType sessionType) {
		this.sessionType = sessionType;
	}
	public Date getStartDate() throws ParseException {
		String[] time = start.split(":");
		int nHr = Integer.parseInt(time[0]);
		int nMin = Integer.parseInt(time[1]);
		int nSec = Integer.parseInt(time[2]);
		Calendar cal = Calendar.getInstance();
		Date date = Clock.getInstance().now();
		return TimeUtil.getScheduledDate(cal, date, nHr, nMin, nSec);
	}
	public void setStart(String start) {
		this.start = start;
	}
	public Date getEndDate() throws ParseException {
		String[] time = end.split(":");
		int nHr = Integer.parseInt(time[0]);
		int nMin = Integer.parseInt(time[1]);
		int nSec = Integer.parseInt(time[2]);
		Calendar cal = Calendar.getInstance();
		Date date = Clock.getInstance().now();
		return TimeUtil.getScheduledDate(cal, date, nHr, nMin, nSec);
	}	

	public void setEnd(String end) {
		this.end = end;
	}
	
	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}
}
