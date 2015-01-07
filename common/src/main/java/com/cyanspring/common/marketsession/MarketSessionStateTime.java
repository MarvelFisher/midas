package com.cyanspring.common.marketsession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.util.TimeUtil;

public class MarketSessionStateTime extends MarketSessionState{
//	private static final Logger log = LoggerFactory
//			.getLogger(MarketSessionStateTime.class);
	
	public MarketSessionStateTime(MarketSessionTime sessionTime) {
		super(sessionTime);
	}

	@Override
	protected MarketSessionEvent createEvent(Date date, MarketSessionTime.SessionData sessionData, MarketSessionTime sessionTime) throws ParseException{
		Date start = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.start);
		Date end = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.end);
		return new MarketSessionEvent(null, null, sessionData.session, start, end);
	}

	protected boolean compareTime(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(sessionTime.getTimeFormat());
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(sdf.parse(compare.start));
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(sdf.parse(compare.end));
		
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);	
		
		startCal.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH));
		endCal.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH));
		
		if(TimeUtil.getTimePass(startCal.getTime(), dateCal.getTime()) <= 0 &&
				TimeUtil.getTimePass(endCal.getTime(), dateCal.getTime()) >= 0)
			return true;
		return false;
	}

}
