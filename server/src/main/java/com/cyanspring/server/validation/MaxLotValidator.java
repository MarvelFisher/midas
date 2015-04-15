package com.cyanspring.server.validation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class MaxLotValidator implements IOrderValidator {
	private static final Logger log = LoggerFactory
			.getLogger(MaxLotValidator.class);
	@Autowired
	private IRefDataManager refDataManager;	

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		try{
			Double qty = (Double)map.get(OrderField.QUANTITY.value());
			if (qty == null)
				return;
			OrderType type = (OrderType)map.get(OrderField.TYPE.value());

			String symbol;
			if(order == null)
				symbol = (String)map.get(OrderField.SYMBOL.value());		
			else
				symbol = order.getSymbol();

			RefData refData = refDataManager.getRefData(symbol);
			if( null == refData ){
				log.warn("This symbol doesn't exist in refData :"+symbol);
				return;
			}
			if(OrderType.Limit.equals(type)){
				
				if(refData.getLimitMaximumLot() == 0)
					return;				
				
				if(qty > refData.getLimitMaximumLot())
					throw new OrderValidationException("The order quantity is over maximun lot",ErrorMessage.ORDER_QTY_OVER_MAX_LOT);			
			}else{
				
				if(refData.getMarketMaximumLot() == 0)
					return;		
				
				if(qty > refData.getMarketMaximumLot())
					throw new OrderValidationException("The order quantity is over maximun lot",ErrorMessage.ORDER_QTY_OVER_MAX_LOT);			
			}			
		}catch(OrderValidationException e){
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

	public IRefDataManager getRefDataManager() {
		return refDataManager;
	}

	public void setRefDataManager(IRefDataManager refDataManager) {
		this.refDataManager = refDataManager;
	}
	
	
}
