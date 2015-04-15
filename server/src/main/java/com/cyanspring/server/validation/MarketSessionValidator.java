package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.message.MessageLookup;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.marketsession.MarketSessionManager;

public class MarketSessionValidator implements IOrderValidator{
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionValidator.class);
	
	@Autowired
	MarketSessionManager marketSessionManager;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		try{
			
			MarketSessionType sessionType =  marketSessionManager.getCurrentSessionType();
			if(sessionType.equals(MarketSessionType.CLOSE)){
				throw new OrderValidationException("Market closed,order couldn't be placed",ErrorMessage.MARKET_CLOSED);
			 }
		}catch(OrderValidationException e){			
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

}
