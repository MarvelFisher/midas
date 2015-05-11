package com.cyanspring.server.livetrading.rule;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.LiveTradingType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.server.livetrading.LiveTradingSetting;

public class DayTrading implements IUserLiveTradingRule{
	
	private static final Logger log = LoggerFactory
			.getLogger(DayTrading.class);
	
	@Autowired
	private LiveTradingSetting liveTradingSetting;
	
	public Map <LiveTradingFieldType,Object> paramsMap;
	
	public DayTrading(Map <LiveTradingFieldType,Object>map ) {
		paramsMap = map;
	}

	@Override
	public AccountSetting setRule(AccountSetting oldAccountSetting, AccountSetting newAccountSetting)
			throws AccountException {
		
		String settingDate = oldAccountSetting.getLiveTradingSettedDate();
		
		if(StringUtils.hasText(settingDate)){
			
			int defaultDays = liveTradingSetting.getChangeSettingFrozenDays();
			
			if(LiveTradingType.DAY_TRADING.equals(oldAccountSetting.getLiveTradingType())
					&& isOverDays(settingDate, defaultDays)){
				
				throw new AccountException("cant change live trading setting , because not over "+defaultDays+" days"
						,ErrorMessage.LIVE_TRADING_SETTING_NOT_OVER_FROZEN_DAYS);

			}
		}
		
		double positionStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.POSITION_STOP_LOSS));
		double frozenStopLoss = Double.parseDouble((String)paramsMap.get(LiveTradingFieldType.FROZEN_STOP_LOSS));
		oldAccountSetting.setStopLossPercent(positionStopLoss);
		oldAccountSetting.setFreezePercent(frozenStopLoss);
		oldAccountSetting.setTerminatePercent(0.0);
		oldAccountSetting.setLiveTrading(newAccountSetting.isLiveTrading());
		oldAccountSetting.setUserLiveTrading(newAccountSetting.isUserLiveTrading());
		oldAccountSetting.setLiveTradingType(LiveTradingType.DAY_TRADING);
		oldAccountSetting.setLiveTradingSettedDate(TimeUtil.formatDate(TimeUtil.getOnlyDate(new Date()), dateFormat));
		
		return oldAccountSetting;
	}
	
	private boolean isOverDays(String date,int days)throws AccountException {
		
		try{
			log.info("check over days {}, days:{}",date,days);
			Date settedDate = TimeUtil.parseDate(date,dateFormat);
			Calendar settedCalendar = Calendar.getInstance();
			settedCalendar.setTime(settedDate);			
			settedCalendar.add(Calendar.DATE, days);	
		
			Date now = TimeUtil.getOnlyDate(new Date());
			log.info("settedDate: {}, now:{}",settedCalendar.getTime(),now);
			if(TimeUtil.getTimePass(now, settedCalendar.getTime())<0){
				return true;
			}else{
				return false;
			}
			
		}catch(Exception e){
			throw new AccountException(e.getMessage(),ErrorMessage.LIVE_TRADING_SETTING_NOT_OVER_FROZEN_DAYS);
		}
		
	}


}
