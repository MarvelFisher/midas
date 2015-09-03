package com.cyanspring.server.validation;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketsession.MarketSessionData;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.marketsession.MarketSessionUtil;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class IndexMarketSessionValidator implements IOrderValidator{

	private static final Logger log = LoggerFactory.getLogger(IndexMarketSessionValidator.class);
	
	@Autowired
	MarketSessionUtil marketSessionUtil;

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
			
			if(marketSessionUtil.isNotInOrderAcceptableTime(new Date(), symbol)){
				throw new OrderValidationException("Market closed,order couldn't be placed",ErrorMessage.MARKET_CLOSED);
			}
			
		}catch(OrderValidationException e){			
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
