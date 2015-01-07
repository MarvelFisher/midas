package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.TimeUtil;

public abstract class MarketSessionState {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionState.class);
	private MarketSessionState failNext;
	private MarketSessionState successNext;
	protected MarketSessionEvent currentMarketSessionEvent;
	protected MarketSessionTime sessionTime;
	
	public MarketSessionState(MarketSessionTime sessionTime){
		this.sessionTime = sessionTime;
	}
	
	public boolean isChanged(Date date){
		if(currentMarketSessionEvent == null)
			return true;
		return TimeUtil.getTimePass(date, currentMarketSessionEvent.getEnd()) >= 0 ||
				   TimeUtil.getTimePass(date, currentMarketSessionEvent.getStart()) <= 0;
	}
	
	public MarketSessionEvent getCurrentMarketSessionEvent(Date date) throws ParseException{
		if(currentMarketSessionEvent == null)
			return calCurrentMarketSession(date);		
		if(!isChanged(date))
			return currentMarketSessionEvent;
		return calCurrentMarketSession(date);		
	}
	
	protected void goSuccess(Date date) throws ParseException{
		if(successNext != null)
			currentMarketSessionEvent = successNext.calCurrentMarketSession(date);
	}
	
	protected void goFail(Date date) throws ParseException{
		if(failNext != null)
			currentMarketSessionEvent = failNext.calCurrentMarketSession(date);
	}
	
	protected MarketSessionEvent calCurrentMarketSession(Date date) throws ParseException{
		if(sessionTime.lst != null){
			for(MarketSessionTime.SessionData sessionData: sessionTime.lst){
				if(compareTime(sessionTime, sessionData, date)){
					currentMarketSessionEvent = createEvent(date, sessionData, sessionTime);
					goSuccess(date);
					return currentMarketSessionEvent; // find and return;
				}
			}
		}else{
			log.error("No MarketSessionTime data set in: " + sessionTime + "[" + sessionTime.hashCode() + "]");
		}
		goFail(date);
		return currentMarketSessionEvent; 
	}		
	
	protected abstract boolean compareTime(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException;
	protected abstract MarketSessionEvent createEvent(Date date, MarketSessionTime.SessionData sessionData, MarketSessionTime sessionTime) throws ParseException;
	
	public void setFailNext(MarketSessionState failNext) {
		this.failNext = failNext;
	}
	
	public void setSuccessNext(MarketSessionState successNext) {
		this.successNext = successNext;
	}
}
