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

public class MarketSessionStateTime extends MarketSessionState{
//	private static final Logger log = LoggerFactory
//			.getLogger(MarketSessionStateTime.class);
	
	public MarketSessionStateTime(MarketSessionTime sessionTime){
		super(sessionTime);
	}

	@Override
	protected MarketSessionEvent createEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException{
		Date start = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.start);
		Date end = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.end);
		return new MarketSessionEvent(null, null, sessionData.session, start, end, tradeDate, Default.getMarket());
	}

	private void saveTradeDate(Date date) {
		String[] times = Default.getTradeDateTime().split(":");	
		int nHour = Integer.parseInt(times[0]);
		int nMin = Integer.parseInt(times[1]);
		int nSecond = Integer.parseInt(times[2]);
		
		Calendar cal = Default.getCalendar();
		Date scheduledToday = TimeUtil.getScheduledDate(cal, date, nHour, nMin, nSecond);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		if(tradeDate == null && nextTradeDate == null){
			tradeDate = sdf.format(date);
			nextTradeDate = tradeDate;
			tradeDateUpdate = true;
			return;
		}
		
		if(TimeUtil.getTimePass(date, scheduledToday) > 0 && !tradeDate.equals(nextTradeDate)){
			tradeDate = nextTradeDate;
			tradeDateUpdate = true;
		}else{			
			tradeDateUpdate = false;						
		}
		nextTradeDate = sdf.format(date);
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
				TimeUtil.getTimePass(endCal.getTime(), dateCal.getTime()) >= 0){
			saveTradeDate(date);
			return true;
		}			
		return false;
	}

}
