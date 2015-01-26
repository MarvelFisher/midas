package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.Default;
import com.cyanspring.common.event.marketsession.MarketSessionEvent;


public class MarketSessionStateDay extends MarketSessionState{
//	private static final Logger log = LoggerFactory
//			.getLogger(MarketSessionStateDay.class);
	
	public MarketSessionStateDay(MarketSessionTime sessionTime){
		super(sessionTime);
	}
	
	@Override
	protected MarketSessionEvent createEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException{
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
	protected boolean compareTime(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat(sessionTime.getTimeFormat());
		Calendar compareCal = Calendar.getInstance();
		compareCal.setTime(sdf.parse(compare.date));
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		if(compareCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
				compareCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) &&
				compareCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH))
			return true;
		return false;
	}
}
