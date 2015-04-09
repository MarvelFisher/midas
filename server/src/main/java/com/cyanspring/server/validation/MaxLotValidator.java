package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class MaxLotValidator implements IOrderValidator {
	
	@Autowired
	IRefDataManager refDataManager;	

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		Double qty = (Double)map.get(OrderField.QUANTITY.value());
		if (qty == null)
			return;
		String symbol;
		if(order == null)
			symbol = (String)map.get(OrderField.SYMBOL.value());		
		else
			symbol = order.getSymbol();
		
		RefData refData = refDataManager.getRefData(symbol);
		if(refData.getMaximumLot() == 0)
			return;
		
		if(qty > refData.getMaximumLot())
			throw new OrderValidationException("The order quantity is over maximun lot",ErrorMessage.ORDER_QTY_OVER_MAX_LOT);
		
	}

}
