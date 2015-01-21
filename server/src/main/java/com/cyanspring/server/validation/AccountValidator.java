package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;

public class AccountValidator implements IFieldValidator {

	@Autowired(required=false)
	private AccountKeeper accountKeeper;
	
	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		try {
			if(null == accountKeeper)
				return;
			
			Account account = accountKeeper.getAccount((String)value);
			if(null == account) {
				throw new OrderValidationException("Account doesn't exist: " + account.getId());
			}
			
			String user = (String)map.get(OrderField.USER.value());
			if(null != user && !user.equals(account.getUserId())) {
				throw new OrderValidationException("Account and user not match: " + account.getId() + ", " + user);
			}
		} catch(Exception e) {
			throw new OrderValidationException(e.getMessage());
		}
	}

}
