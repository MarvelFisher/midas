package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.marketsession.MarketSessionManager;

public class MarketSessionValidator implements IOrderValidator{
	
	
	@Autowired
	MarketSessionManager marketSessionManager;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException{
		try{
			MarketSessionType sessionType =  marketSessionManager.getCurrentSessionType();
			if(sessionType.equals(MarketSessionType.CLOSE)){
				throw new OrderValidationException("Market closed,order couldn't be placed");
			 }
		}catch(Exception e){			
			throw new OrderValidationException("Error:"+e.getMessage());
		}
		
	}

}
