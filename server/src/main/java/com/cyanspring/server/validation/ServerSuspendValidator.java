package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.validation.data.ValidationDataProvider;

public class ServerSuspendValidator implements IOrderValidator {
	
	@Autowired(required = true)
	private ValidationDataProvider validationDataProvider;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order) throws OrderValidationException {
		if(validationDataProvider.isServerSuspend())
			throw new OrderValidationException("Server is suspend", ErrorMessage.SERVER_SUSPEND);
	}

}
