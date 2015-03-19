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
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	
	public MarketSessionStateTime(MarketSessionTime sessionTime){
		super(sessionTime);
	}

	@Override
	protected MarketSessionEvent createMarketSessionEvent(MarketSessionTime sessionTime, MarketSessionTime.SessionData sessionData, Date date) throws ParseException{
		isHoliday = false;
		if(super.calTradeDate && sessionData.session.equals(MarketSessionType.PREOPEN))
			setTradeDate(date);
		Date start = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.start);
		Date end = TimeUtil.parseTime(sessionTime.getTimeFormat(), sessionData.end);
		return new MarketSessionEvent(null, null, sessionData.session, start, end, tradeDate, Default.getMarket());
	}

	protected boolean checkState(MarketSessionTime sessionTime, MarketSessionTime.SessionData compare, Date date) throws ParseException {
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
			return true;
		}			
		return false;
	}

	@Override
	protected Date tradeDateInit(Date date, MarketSessionType type) throws ParseException {
		for(MarketSessionTime.SessionData sessionData: sessionTime.lst){
			if(sessionData.session.equals(MarketSessionType.PREOPEN)){
				String[] st = sessionData.start.split(":");				
				int nHour = Integer.parseInt(st[0]);
				int nMin = Integer.parseInt(st[1]);
				int nSecond = Integer.parseInt(st[2]);
				if(TimeUtil.getTimePass(Clock.getInstance().now(), TimeUtil.getScheduledDate(Calendar.getInstance(),
						date, nHour, nMin, nSecond)) >= 0){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					tradeDate = sdf.format(date);
				}
			}
		}		
		return date;
	}
}
