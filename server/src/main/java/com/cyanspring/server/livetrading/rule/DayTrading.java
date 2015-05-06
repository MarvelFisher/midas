package com.cyanspring.server.livetrading.rule;

import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.livetrading.LiveTradingException;

public class DayTrading implements IUserLiveTradingRule{

	private final String dateFormat = "yyyy-MM-dd";

	
	
	@Override
	public AccountSetting setRule(Account account,AccountSetting accountSetting)
			throws LiveTradingException {
		
		return null;
	}
	
	private boolean isOverWeeks(String date,int weeks)throws LiveTradingException {
		
		try{
			
			Date settedDate = TimeUtil.parseDate(date,dateFormat);
			Calendar settedCalendar = Calendar.getInstance();
			settedCalendar.setTime(settedDate);			
			settedCalendar.add(Calendar.MONTH, weeks);			
			Date now = TimeUtil.getOnlyDate(new Date());
			if(TimeUtil.getTimePass(now, settedCalendar.getTime())>0){
				return true;
			}else{
				return false;
			}
			
		}catch(Exception e){
			throw new LiveTradingException(e.getMessage());
		}
		
	}


}
