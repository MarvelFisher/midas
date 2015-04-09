package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.UserKeeper;

public class UserValidator implements IFieldValidator {

	@Autowired(required=false)
	private UserKeeper userKeeper;
	
	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		try {
			if(null != userKeeper && !userKeeper.userExists((String)value)) {
				throw new OrderValidationException("User doesn't exist: " + value,ErrorMessage.ACCOUNT_NOT_EXIST);
			}
		} catch(OrderValidationException e){
			throw e;
		} catch(Exception e) {
			throw new OrderValidationException(e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}
	}

}
