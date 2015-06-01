package com.cyanspring.server.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketsession.MarketSessionChecker;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.TradeDateManager;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.BusinessManager;
/***
 * 
 * @author Jimmy.Cheng
 *
 */
public class AllowPlaceOrderTimeValidator implements IOrderValidator{
	
	private static final Logger log = LoggerFactory
			.getLogger(AllowPlaceOrderTimeValidator.class);
	
	@Autowired
	TradeDateManager tradeDateManager;
	
	@Autowired
	MarketSessionChecker sessionChecker;
	
	@Autowired
	BusinessManager businessManager;
	
	private int howManyMinutes;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		try {
			
			Date today = new Date();
			Date saturday = getSaturdayOfWeek(today);		
			Date firstTradeDate = tradeDateManager.nextTradeDate(saturday);
			MarketSessionData session = getOpenMarketSession(firstTradeDate);
			if(null == session || !session.getSessionType().equals(MarketSessionType.OPEN)){
				return;
			}			
			
			Date minutesAgo = getMinutesAgo(session.getStartDate());			
			if(checkInterval(today,saturday,minutesAgo)){
				throw new OrderValidationException("Your order can’t be placed. We start taking orders at 1 hour before market open.",ErrorMessage.MARKET_WILL_TAKE_ORDER_BEFORE_OPEN_ONE_HOUR);
			}
		} catch (OrderValidationException e){
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}		
	}
	private Date getMinutesAgo(Date startDate) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.add(Calendar.MINUTE, -getHowManyMinutes());
		return cal.getTime();
	}
	private MarketSessionData getOpenMarketSession(Date firstTradeDate) throws Exception {

		Date today = TimeUtil.getOnlyDate(firstTradeDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(today);
		tomorrow.add(Calendar.DATE, 1);
		
		MarketSessionData data = null;	
		while(true){
			
			if(cal.getTimeInMillis() > tomorrow.getTimeInMillis()){
				break;
			}
			data = sessionChecker.getState(cal.getTime(), null);	
			if(null == data){
				break;
			}	
			if(!MarketSessionType.OPEN.equals(data.getSessionType())){
				cal.setTime(data.getEndDate());
				cal.add(Calendar.MINUTE, 1);
				continue;
			}else{
				break;
			}
		}	
		
		return data;
	}
	private  Date getSaturdayOfWeek(Date date) throws ParseException{
		
		Calendar cal = Calendar.getInstance();
		int sat = Calendar.SATURDAY;
		cal.setTime(date);
		int today = cal.get(Calendar.DAY_OF_WEEK);

		if(today != sat){	
			int interval = today ;
			cal.add(Calendar.DATE, -interval);
		}
		
		String cancelOpenOrderTime = businessManager.getCancelPendingOrderTime();	
		if(StringUtils.hasText(cancelOpenOrderTime)){
			
			Date time = timeFormat.parse(cancelOpenOrderTime);
			Calendar timeCal = Calendar.getInstance();
			timeCal.setTime(time);
			cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
		}
		
		return cal.getTime();
	}
	private boolean checkInterval(Date now,Date start, Date end) throws ParseException {

		if(TimeUtil.getTimePass(now, start)>=0 && TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}
	public int getHowManyMinutes() {
		return howManyMinutes;
	}
	public void setHowManyMinutes(int howManyMinutes) {
		this.howManyMinutes = howManyMinutes;
	}
}
