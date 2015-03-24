package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;

public class HoldValidator implements IFieldValidator {

	@Autowired
	AccountKeeper accountKeeper;
	
	@Autowired
	private PositionKeeper positionKeeper;
	
	@Autowired
	IRefDataManager refDataManager;
	
	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		if(null == accountKeeper)
			return;
		
		Account account = accountKeeper.getAccount((String)value);
		if(null == account) {
			return;
		}
		
		OpenPosition positions = positionKeeper.getOverallPosition(account, order.getSymbol());
		double qty = positions.getQty();
		
		RefData refData = refDataManager.getRefData(order.getSymbol());
		double maxHold = refData.getMaximumHold();
		if(PriceUtils.isZero(maxHold))
			return;
		if(order.getSide().equals(OrderSide.Buy)){
			if(maxHold < Math.abs(qty + order.getQuantity()))
				throw new OrderValidationException("The order quantity is over maximun hold.");			
		}else{
			if(maxHold < Math.abs(qty - order.getQuantity()))
				throw new OrderValidationException("The order quantity is over maximum hold.");	
		}
	}

}
