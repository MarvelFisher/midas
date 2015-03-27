package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class LotValidator implements IOrderValidator {
	
	@Autowired
	IRefDataManager refDataManager;	

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		RefData refData = refDataManager.getRefData(order.getSymbol());
		if(refData.getMaximumLot() == 0)
			return;
		if(order.getQuantity() > refData.getMaximumLot())
			throw new OrderValidationException("The order quantity is over maximun lot");
		
	}

}
