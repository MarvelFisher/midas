package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.LiveTradingChecker;
import com.cyanspring.server.account.LiveTradingChecker.LiveTradingState;
import com.cyanspring.server.account.RiskManager;

public class LiveTradingValidator implements IOrderValidator{
	private static final Logger log = LoggerFactory
			.getLogger(LiveTradingValidator.class);
	
	@Autowired
	RiskManager riskManager;

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		try{
			log.info("into LiveTradingValidator");
			LiveTradingChecker liveTradingChecker = riskManager.getLiveTradingChecker();
			
			if( null == liveTradingChecker || !liveTradingChecker.isStartLiveTrading()){
				return ;
			}
			
			LiveTradingState state =  liveTradingChecker.getStatus();
			
			if(state.equals(LiveTradingState.ON_CLOSING_POSITION)){
				
				throw new OrderValidationException("Live trading on closing position",ErrorMessage.LIVE_TRADING_ON_CLOSING_ALL_POSITION);
			
			}
		}catch(OrderValidationException e){			
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}		
	}

}
