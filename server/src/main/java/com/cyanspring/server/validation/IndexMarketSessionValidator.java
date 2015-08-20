package com.cyanspring.server.validation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionManager;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.validation.bean.AvailableTimeBean;

public class IndexMarketSessionValidator implements IOrderValidator{

	private static final Logger log = LoggerFactory.getLogger(IndexMarketSessionValidator.class);
	
	@Autowired
	MarketSessionUtil marketSessionUtil;
	
	@Autowired(required=false)
	@Qualifier("availableTimeList")
	ArrayList <AvailableTimeBean> availableTimeList;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		try{
			
			String symbol;
			if(order == null){				
				symbol = (String)map.get(OrderField.SYMBOL.value());
			}else{			
				symbol = order.getSymbol();
			}
			
			if( null == symbol)
				return;
			
			MarketSessionData data = marketSessionUtil.getCurrentMarketSession(symbol);
			
			if( null == data){
				log.warn("can't get index market session:{}",symbol);
				return;
			}
						
			MarketSessionType sessionType =  data.getSessionType();
			if(sessionType.equals(MarketSessionType.CLOSE) || sessionType.equals(MarketSessionType.PRECLOSE)){
				throw new OrderValidationException("Market closed,order couldn't be placed",ErrorMessage.MARKET_CLOSED);
			 }
			
			if( null != availableTimeList && availableTimeList.size()>0){
				 checkAvailableTime(availableTimeList,sessionType);
			 }
			
		}catch(OrderValidationException e){			
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

	private void checkAvailableTime(List<AvailableTimeBean> availableTimeList,MarketSessionType currentSessionType)throws OrderValidationException {
		
		for(AvailableTimeBean bean:availableTimeList){
			
			MarketSessionType type = bean.getMarketSession();
			String startTime = bean.getStartTime();
			String endTime = bean.getEndTime();
			
			if(null == type || !type.equals(currentSessionType)){
				continue;
			}
			if(!StringUtils.hasText(startTime) && !StringUtils.hasText(endTime)){
				continue;
			}
			try{
				boolean isInTheTime = false;
				
				if(StringUtils.hasText(startTime) && StringUtils.hasText(endTime)){
					isInTheTime = checkInterval(startTime,endTime);
				}else if(!StringUtils.hasText(startTime) && StringUtils.hasText(endTime)){
					isInTheTime = checkBefore(endTime);
				}else if(StringUtils.hasText(startTime) && !StringUtils.hasText(endTime)){
					isInTheTime = checkAfter(startTime);
				}
				
				if(isInTheTime){
					throw new OrderValidationException("Your order can’t be placed. We start to take orders at 9:10 am.",ErrorMessage.MARKET_WILL_TAKE_ORDER_AFTER_OPEN);
				}
			}catch(OrderValidationException e){
				throw e;
			}catch(ParseException e){
				log.error(e.getMessage(),e);
			}catch(Exception e){
				log.error(e.getMessage(),e);
			}		
		}
	}
	private boolean checkAfter(String startTime) throws ParseException {
		
		Date start = TimeUtil.parseTime("HH:mm:ss", startTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, start)>=0){
			return true;
		}else{
			return false;
		}
	}

	private boolean checkBefore(String endTime) throws ParseException {
		
		Date end = TimeUtil.parseTime("HH:mm:ss", endTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}

	private boolean checkInterval(String startTime, String endTime) throws ParseException {
		
		Date start = TimeUtil.parseTime("HH:mm:ss", startTime);
		Date end = TimeUtil.parseTime("HH:mm:ss", endTime);
		Date now = new Date();
		if(TimeUtil.getTimePass(now, start)>=0 && TimeUtil.getTimePass(now, end)<=0){
			return true;
		}else{
			return false;
		}
	}
}
