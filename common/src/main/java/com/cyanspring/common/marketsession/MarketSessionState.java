package com.cyanspring.common.marketsession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class MarketSessionState {
	private static final Logger log = LoggerFactory.getLogger(MarketSessionState.class);
	private MarketSessionState outState;
	private MarketSessionState inState;
	private String filePath;	
	protected MarketSessionEvent currentMarketSessionEvent;
	protected MarketSessionTime sessionTime;
//	protected static String nextTradeDate;
	
	protected static String tradeDate;
	private static XStream xstream;
	private static File file;
	private static boolean calTradeDate;
	private static boolean tradeDateUpdate;
		
	protected abstract boolean checkState(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException;
	protected abstract MarketSessionEvent createMarketSessionEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException;
	
	public MarketSessionState(MarketSessionTime sessionTime){
		this.sessionTime = sessionTime;
	}
	
	public void init() throws Exception{		
		if(calTradeDate){
			log.info("initialising with " + filePath);
			xstream = new XStream(new DomDriver());
			file = new File(filePath);
			if(file.exists()){
				tradeDate = (String) xstream.fromXML(file);
				tradeDateUpdate = true;
			}else{
				throw new Exception("Missing refdata file: " + filePath);
			}	
		}
	}
	
	private void setTradeDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String newTradeDate = sdf.format(date);
		if(!tradeDate.equals(newTradeDate)){
			tradeDate = newTradeDate;
			tradeDateUpdate = true;
			try(FileOutputStream os = new FileOutputStream(file)) {
				xstream.toXML(tradeDate, os);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}		
		}
	}	
	
	public boolean isStateChanged(Date date){
		if(currentMarketSessionEvent == null)
			return true;
		return TimeUtil.getTimePass(date, currentMarketSessionEvent.getEnd()) >= 0 ||
				   TimeUtil.getTimePass(date, currentMarketSessionEvent.getStart()) <= 0;
	}
	
	public boolean isTradeDateChange() throws ParseException{		
		return tradeDateUpdate;
	}
	
	public void setTradeDateUpdated(){
		tradeDateUpdate = false;
	}
	
	public MarketSessionEvent getCurrentMarketSessionEvent(Date date) throws ParseException{
		if(currentMarketSessionEvent == null)
			return createMarketSessionEvent(date);		
		if(!isStateChanged(date))
			return currentMarketSessionEvent;
		return createMarketSessionEvent(date);		
	}
	
	private void createMarketSessionEvent(Date date, boolean in) throws ParseException{
		if(in){
			if(inState != null)
				currentMarketSessionEvent = inState.createMarketSessionEvent(date);
		}else{
			if(outState != null)
				currentMarketSessionEvent = outState.createMarketSessionEvent(date);
		}
	}
	
	public MarketSessionEvent createMarketSessionEvent(Date date) throws ParseException{
		if(sessionTime.lst != null){
			for(MarketSessionTime.SessionData sessionData: sessionTime.lst){
				if(checkState(sessionTime, sessionData, date)){
					currentMarketSessionEvent = createMarketSessionEvent(sessionTime, sessionData, date);
					createMarketSessionEvent(date, true);
					if(calTradeDate && currentMarketSessionEvent.getSession().equals(MarketSessionType.PREOPEN))
						setTradeDate(date);
					return currentMarketSessionEvent; // find and return;
				}
			}
		}else{
			log.error("No MarketSessionTime data set in: " + sessionTime + "[" + sessionTime.hashCode() + "]");
		}
		createMarketSessionEvent(date, false);
		return currentMarketSessionEvent; 
	}		
	
	public MarketSessionType getCurrentMarketSession(Date date){
		try {
			createMarketSessionEvent(date);
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
		}		
		return currentMarketSessionEvent.getSession();
	}
	
	public void setOutState(MarketSessionState outState) {
		this.outState = outState;
	}
	
	public void setInState(MarketSessionState inState) {
		this.inState = inState;
	}
	
	public String getTradeDate(){
		return tradeDate;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public void setCalTradeDate(boolean calTradeDate) {
		this.calTradeDate = calTradeDate;
	}
}
