package com.cyanspring.server.livetrading.rule;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.livetrading.LiveTradingException;
import com.cyanspring.server.livetrading.LiveTradingSetting;

public class DayTrading implements IUserLiveTradingRule{
	
	private static final Logger log = LoggerFactory
			.getLogger(DayTrading.class);
	
	@Autowired
	private LiveTradingSetting liveTradingSetting;

	private final String dateFormat = "yyyy-MM-dd";
	
	public Map <LiveTradingFieldType,Object> paramsMap;
	
	public DayTrading(Map <LiveTradingFieldType,Object>map ) {
		paramsMap = map;
	}

	@Override
	public AccountSetting setRule(Account account,AccountSetting accountSetting)
			throws LiveTradingException {
		
		log.info("Accout:{} Set Live Trading: DAY_TRADING",account.getId());
		String settingDate = "";
		int defaultWeeks = liveTradingSetting.getChangeSettingFrozenWeeks();
		if(isOverWeeks(settingDate, defaultWeeks)){
			throw new LiveTradingException("cant change live trading setting , because not over "+defaultWeeks+" weeks");
		}else{
			
		}
		
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
