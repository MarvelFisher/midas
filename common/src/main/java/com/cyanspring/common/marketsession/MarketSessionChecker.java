package com.cyanspring.common.marketsession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.TimeUtil;

public class MarketSessionChecker implements IMarketSession{
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionChecker.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Date tradeDate;
	private Map<String, MarketSession> stateMap;
	private ITradeDate tradeDateManager;
	private MarketSessionType currentType;
	 
	@Override
	public void init(Date date) throws Exception {
		if(tradeDateManager != null){			
			String currentIndex = getCurrentIndex(date);
			MarketSession session = stateMap.get(currentIndex);
			if(session == null)
				session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
			for(MarketSessionData data : session.getSessionDatas()){
				if(data.getSessionType().equals(MarketSessionType.PREOPEN)){
					if(TimeUtil.getTimePass(date, data.getStartDate()) > 0){
						tradeDate = date;
						return;
					}
				}
			}
			tradeDate = tradeDateManager.preTradeDate(date);
		}		
	}
	@Override
	public MarketSessionData getState(Date date) throws Exception {
		MarketSessionData sessionData = null;
		String currentIndex = getCurrentIndex(date);
		MarketSession session = stateMap.get(currentIndex);
		if(session == null)
			session = stateMap.get(MarketSessionIndex.DEFAULT.toString());
		for(MarketSessionData data : session.getSessionDatas()){
			if(compare(data, date)){
				sessionData = new MarketSessionData(data.getSessionType(), data.getStart(), data.getEnd());
				if(data.getSessionType().equals(MarketSessionType.PREOPEN) && tradeDateManager != null){
					if(currentType != null && !currentType.equals(data.getSessionType()))
						tradeDate = tradeDateManager.nextTradeDate(tradeDate);
				}
				currentType = data.getSessionType();
			}			
		}
		return sessionData;
	}
	
	private String getCurrentIndex(Date date){
		if(tradeDateManager != null){
			Date preDate = TimeUtil.getPreviousDay(date);
			if(tradeDateManager.isHoliday(date) && tradeDateManager.isHoliday(preDate)){
				return MarketSessionIndex.HOLIDAY.toString();
			}else if(tradeDateManager.isHoliday(date) && !tradeDateManager.isHoliday(preDate)){
				return MarketSessionIndex.HOLIDAY_AFTER_WOERKDAY.toString();
			}else if(!tradeDateManager.isHoliday(date) && tradeDateManager.isHoliday(preDate)){
				return MarketSessionIndex.WORKDAY_AFTER_HOLIDAY.toString();
			}else{
				return MarketSessionIndex.DEFAULT.toString();
			}
		}else{
			return MarketSessionIndex.DEFAULT.toString();
		}
	}
	
	private boolean compare(MarketSessionData data, Date compare) throws ParseException{
		
		if(TimeUtil.getTimePass(data.getStartDate(), compare) <= 0 &&
				TimeUtil.getTimePass(data.getEndDate(), compare) >= 0){
			return true;
		}
		return false;
	}
	
	public String getTradeDate() {
		return sdf.format(this.tradeDate);
	}
	
	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}
	public ITradeDate getTradeDateManager() {
		return tradeDateManager;
	}
	public void setTradeDateManager(ITradeDate tradeDateManager) {
		this.tradeDateManager = tradeDateManager;
	}
	public Map<String, MarketSession> getStateMap() {
		return stateMap;
	}
	public void setStateMap(Map<String, MarketSession> stateMap) {
		this.stateMap = stateMap;
	}
}
