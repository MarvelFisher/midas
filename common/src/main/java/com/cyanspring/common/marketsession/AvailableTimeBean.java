package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.TimeUtil;

public class AvailableTimeBean {
	
	private MarketSessionType marketSession;
	private String startTime;
	private String endTime;
	private boolean isSessionBefore;
	private boolean isSessionStart;
	private int howManyMinutes;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			
	public boolean isTimeInterval(){
		if(StringUtils.hasText(startTime) || StringUtils.hasText(endTime))
			return true;
		
		return false;
	}
	
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

	public int getHowManyMinutes() {
		return howManyMinutes;
	}

	public void setHowManyMinutes(int howManyMinutes) {
		this.howManyMinutes = howManyMinutes;
	}
	
	public boolean isSessionBefore() {
		return isSessionBefore;
	}

	public void setSessionBefore(boolean isSessionBefore) {
		this.isSessionBefore = isSessionBefore;
	}

	public boolean isSessionStart() {
		return isSessionStart;
	}

	public void setSessionStart(boolean isSessionStart) {
		this.isSessionStart = isSessionStart;
	}

	public Date getMinutesAgo(Date startDate) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.MINUTE, -getHowManyMinutes());
		return cal.getTime();
	}

	public boolean checkAfter(String startTime) throws ParseException {
		
		Date start = TimeUtil.parseTime("HH:mm:ss", startTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, start)>=0){
			return true;
		}else{
			return false;
		}
	}

	public boolean checkBefore(String endTime) throws ParseException {
		
		Date end = TimeUtil.parseTime("HH:mm:ss", endTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}

	public boolean checkInterval(Date now,Date start, Date end) throws ParseException {

		if(TimeUtil.getTimePass(now, start)>=0 && TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean checkInterval(String startTime, String endTime) throws ParseException {
		
		Date start = TimeUtil.parseTime("HH:mm:ss", startTime);
		Date end = TimeUtil.parseTime("HH:mm:ss", endTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, start)>=0 && TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}
}
