package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;

public class HoldValidator implements IOrderValidator {

	@Autowired
	AccountKeeper accountKeeper;
	
	@Autowired
	private PositionKeeper positionKeeper;
	
	@Autowired
	IRefDataManager refDataManager;

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if(null == accountKeeper)
			return;
		
		String orderAccount;
		String symbol;
		OrderSide side;
		double quantity;
		
		if(order == null){
			orderAccount = (String) map.get(OrderField.ACCOUNT.value());
			symbol = (String) map.get(OrderField.SYMBOL.value());
			side = (OrderSide) map.get(OrderField.SIDE.value());
			quantity = (Double) map.get(OrderField.QUANTITY.value());
		}else{
			orderAccount = order.getAccount();
			symbol = order.getSymbol();
			side = order.getSide();
			quantity = order.getQuantity();	
		}		
		
		Account account = accountKeeper.getAccount(orderAccount);
		if(null == account) {
			return;
		}
				
		OpenPosition positions = positionKeeper.getOverallPosition(account, symbol);
		double qty = positions.getQty();
		
		RefData refData = refDataManager.getRefData(symbol);
		double maxHold = refData.getMaximumHold();
		if(PriceUtils.isZero(maxHold))
			return;
			
		if(side.equals(OrderSide.Buy)){
			if(maxHold < Math.abs(qty + quantity))
				throw new OrderValidationException("The order quantity is over maximun hold.");			
		}else{
			if(maxHold < Math.abs(qty - quantity))
				throw new OrderValidationException("The order quantity is over maximum hold.");	
		}		
	}
}
