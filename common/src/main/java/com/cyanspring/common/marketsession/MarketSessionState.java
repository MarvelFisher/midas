package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.util.TimeUtil;

public abstract class MarketSessionState {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionState.class);
	private MarketSessionState failNext;
	private MarketSessionState successNext;
	protected MarketSessionEvent currentMarketSessionEvent;
	protected MarketSessionTime sessionTime;
	protected static String tradeDate;
	protected static String nextTradeDate;
	protected static boolean tradeDateUpdate;
	
	public MarketSessionState(MarketSessionTime sessionTime){
		this.sessionTime = sessionTime;
	}
	
	public void init() throws ParseException{
		tradeDateUpdate = false;
		Date date = Clock.getInstance().now();
		String[] times = Default.getTradeDateTime().split(":");	
		int nHour = Integer.parseInt(times[0]);
		int nMin = Integer.parseInt(times[1]);
		int nSecond = Integer.parseInt(times[2]);		
		Calendar cal = Default.getCalendar();
		while(tradeDate == null){
			calTradeDate(date);
			date = TimeUtil.getPreviousDay(date);
			date = TimeUtil.getScheduledDate(cal, date, nHour, nMin, nSecond+1);
		}
	}
	
	public boolean isStateChanged(Date date){
		if(currentMarketSessionEvent == null)
			return true;
		return TimeUtil.getTimePass(date, currentMarketSessionEvent.getEnd()) >= 0 ||
				   TimeUtil.getTimePass(date, currentMarketSessionEvent.getStart()) <= 0;
	}
	
	public boolean isTradeDateChange(Date date) throws ParseException{		
		calTradeDate(date);
		return tradeDateUpdate;
	}
	
	public MarketSessionEvent getCurrentMarketSessionEvent(Date date) throws ParseException{
		if(currentMarketSessionEvent == null)
			return calCurrentMarketSession(date);		
		if(!isStateChanged(date))
			return currentMarketSessionEvent;
		return calCurrentMarketSession(date);		
	}
	
	protected void createSuccessEvent(Date date) throws ParseException{
		if(successNext != null)
			currentMarketSessionEvent = successNext.calCurrentMarketSession(date);
	}
	
	protected void createFailEvent(Date date) throws ParseException{
		if(failNext != null)
			currentMarketSessionEvent = failNext.calCurrentMarketSession(date);
	}
	
	protected void goSuccess(Date date) throws ParseException{
		if(successNext != null)
			successNext.calTradeDate(date);
	}
	
	protected void goFail(Date date) throws ParseException{
		if(failNext != null)
			failNext.calTradeDate(date);
	}
	
	private void calTradeDate(Date date) throws ParseException{
		if(sessionTime.lst != null){
			for(MarketSessionTime.SessionData sessionData: sessionTime.lst){
				if(compareTime(sessionTime, sessionData, date)){
					goSuccess(date);
					return;
				}
			}
		}else{
			log.error("No MarketSessionTime data set in: " + sessionTime + "[" + sessionTime.hashCode() + "]");
		}
		goFail(date);
	}
	
	protected MarketSessionEvent calCurrentMarketSession(Date date) throws ParseException{
		if(sessionTime.lst != null){
			for(MarketSessionTime.SessionData sessionData: sessionTime.lst){
				if(compareTime(sessionTime, sessionData, date)){
					currentMarketSessionEvent = createEvent(sessionTime, sessionData, date);
					createSuccessEvent(date);
					return currentMarketSessionEvent; // find and return;
				}
			}
		}else{
			log.error("No MarketSessionTime data set in: " + sessionTime + "[" + sessionTime.hashCode() + "]");
		}
		createFailEvent(date);
		return currentMarketSessionEvent; 
	}		
	
	protected abstract boolean compareTime(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException;
	protected abstract MarketSessionEvent createEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException;
	
	public void setFailNext(MarketSessionState failNext) {
		this.failNext = failNext;
	}
	
	public void setSuccessNext(MarketSessionState successNext) {
		this.successNext = successNext;
	}
	
	public String getTradeDate(){
		return tradeDate;
	}
}
