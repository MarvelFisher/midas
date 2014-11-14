package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
			if(null != accountKeeper && !accountKeeper.accountExists((String)value)) {
				throw new OrderValidationException("Account doesn't exist");
			}
		} catch(Exception e) {
			throw new OrderValidationException("Field " + field + " has caused exception: " + e.getMessage());
		}
	}

}
