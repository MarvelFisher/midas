package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.CoinManager;
import com.cyanspring.server.livetrading.LiveTradingSession;

public class LiveTradingSessionValidator implements IOrderValidator {
	
	private static final Logger log = LoggerFactory
			.getLogger(AccountStateValidator.class);
	
	@Autowired
	private LiveTradingSession liveTradingSession;
	@Autowired
	public AccountKeeper accountKeeper;
	@Autowired(required = false)
	public CoinManager coinManager;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		try{
			
			if( null == liveTradingSession || null == accountKeeper){
				return;
			}
			
			String accountId = null;				
			if(order == null){
				accountId = (String)map.get(OrderField.ACCOUNT.value());			
			}else{	
				accountId = order.getAccount();		
			}			
			if(!StringUtils.hasText(accountId)){				
				return;			
			}
			
			Account account = accountKeeper.getAccount(accountId);			
			if( null == account ){
				return;
			}
			
			if( null != coinManager && !coinManager.canCheckDayTradingMode(account.getId()))
				return;

			AccountSetting accountSetting = accountKeeper.getAccountSetting(account.getId());
			if( null != account && accountSetting.isLiveTrading()){			
				
				if(!liveTradingSession.isAllowLiveTrading()){				
					throw new OrderValidationException("Live trading on stop trading time"
							,ErrorMessage.LIVE_TRADING_STOP_TRADING);
				}
				
			}
			
		}catch(OrderValidationException e){		
			throw e;			
		}catch(Exception e){		
			log.error(e.getMessage(),e);		
		}

	}

}
