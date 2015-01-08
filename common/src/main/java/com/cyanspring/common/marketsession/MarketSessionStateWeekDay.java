package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MarketSessionStateWeekDay extends MarketSessionState{
//	private static final Logger log = LoggerFactory
//			.getLogger(MarketSessionStateWeekDay.class);
	
	public MarketSessionStateWeekDay(MarketSessionTime sessionTime) {
		super(sessionTime);
	}
	
	@Override
	protected MarketSessionEvent createEvent(Date date, MarketSessionTime.SessionData sessionData, MarketSessionTime sessionTime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date start = cal.getTime();
		
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date end = cal.getTime();
		
		return new MarketSessionEvent(null, null, sessionData.session, start, end);
	}
	
	@Override
	protected boolean compareTime(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int weekDay = cal.get(Calendar.DAY_OF_WEEK);
		if(weekDay == 1)
			weekDay = 7;
		else 
			weekDay--;
		if(compare.weekDay.equals(String.valueOf(weekDay)))
			return true;
		return false;
	}
}
