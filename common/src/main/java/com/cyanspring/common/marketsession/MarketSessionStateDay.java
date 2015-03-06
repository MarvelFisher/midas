package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.Default;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;
import com.cyanspring.common.util.TimeUtil;


public class MarketSessionStateDay extends MarketSessionState{
//	private static final Logger log = LoggerFactory
//			.getLogger(MarketSessionStateDay.class);
	
	public MarketSessionStateDay(MarketSessionTime sessionTime){
		super(sessionTime);
	}
	
	@Override
	protected MarketSessionEvent createMarketSessionEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException{
		if(sessionData.session.equals(MarketSessionType.CLOSE))
			isHoliday = true;
		else
			isHoliday = false;
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(sessionTime.getTimeFormat());
		Date parseDate = dateFormat.parse(sessionData.date);
		cal.setTime(parseDate);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date start = cal.getTime();
		
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date end = cal.getTime();
		
		return new MarketSessionEvent(null, null, sessionData.session, start, end, tradeDate, Default.getMarket());
	}
	
	@Override
	protected boolean checkState(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException{
//		tradeDateUpdate = false;
		SimpleDateFormat sdf = new SimpleDateFormat(sessionTime.getTimeFormat());
		
		if(TimeUtil.sameDate(date, sdf.parse(compare.date)))
			return true;
		return false;
	}
}
