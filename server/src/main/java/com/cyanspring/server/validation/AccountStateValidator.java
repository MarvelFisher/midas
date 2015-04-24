package com.cyanspring.server.validation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.State;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import static com.cyanspring.common.account.State.*;

public class AccountStateValidator {
	
	private static final Logger log = LoggerFactory
			.getLogger(AccountStateValidator.class);
	
	@Autowired
	public AccountKeeper accountKeeper;
	
	
	public void validate(String id)
			throws OrderValidationException {
		
		try{
			if( null == accountKeeper){
				
				log.warn("accountKeeper not exist");
				return;
				
			}
			
			if(!StringUtils.hasText(id)){
				
				log.warn("doesn't exist any data to validate");
				return;
				
			}
			
			
			Account account = accountKeeper.getAccount(id);
			log.info("\n -------+\n"
					+ "account:"+account.getUserId() +"\n"
					+ "state:"+account.getState()+" \n"
					+ "account value:"+account.getValue()+"\n"
					+ "daily pnl:"+account.getDailyPnL()+"\n"
					+ "------------"
					);
			
			
			if( null != account){
				
				State state = account.getState();

				if(FROZEN == state){
					
					throw new OrderValidationException("Exceed Daily Maximun Loss!  Your account will be frozen for the rest of the day."
							,ErrorMessage.ACCOUNT_FROZEN);

				}else if(TERMINATED == state){
					
					throw new OrderValidationException("Exceed Account Stop Loss!  Your account is terminated."
							,ErrorMessage.ACCOUNT_TERMINATED);

				}
				
			}
		
		}catch(OrderValidationException e){	
			
			throw e;
			
		}catch(Exception e){
			
			log.error(e.getMessage(),e);
			
		}

		
		
	}

}
