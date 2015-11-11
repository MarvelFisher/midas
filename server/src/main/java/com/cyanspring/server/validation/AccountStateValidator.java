package com.cyanspring.server.validation;

import static com.cyanspring.common.account.AccountState.FROZEN;
import static com.cyanspring.common.account.AccountState.TERMINATED;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class AccountStateValidator  implements IOrderValidator{
	
	private static final Logger log = LoggerFactory
			.getLogger(AccountStateValidator.class);
	
	@Autowired
	public AccountKeeper accountKeeper;

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		try{
			if( null == accountKeeper){			
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
						
			if( null != account){
				
				AccountState state = account.getState();

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
