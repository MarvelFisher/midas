package com.cyanspring.server.validation;

import java.util.Map;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.OrderUtils;
import com.cyanspring.common.validation.OrderValidationException;

public class StopLossPriceValidator implements IFieldValidator {

	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {	
		try {
			Object obj = map.get(OrderField.STOP_LOSS_PRICE.value());
			if(null == obj)
				return;
			
			double stopLossPrice = (Double)obj;
			if(PriceUtils.isZero(stopLossPrice))
				throw new OrderValidationException("Stop loss price can't be 0",ErrorMessage.STOP_LOSS_PRICE_EMPTY);

			OrderType orderType;
			if(null != order) {
				orderType = order.getOrderType();
			} else {
				orderType = (OrderType)map.get(OrderField.TYPE.value());
			}
			
			if(orderType.equals(OrderType.Market))
				return;
			
			OrderSide orderSide;
			double limitPrice;
			if(null != order) {
				orderSide = order.getSide();
				limitPrice = order.getPrice();
			} else {
				orderSide = (OrderSide)map.get(OrderField.SIDE.value());
				limitPrice = (Double)map.get(OrderField.PRICE.value());
			}
			
			if(OrderUtils.isBetterPrice(orderSide, limitPrice, stopLossPrice))
				throw new OrderValidationException("Stop loss price " + stopLossPrice + 
						" can not be more aggressive than limit price " + limitPrice,ErrorMessage.STOP_LOSS_PRICE_CANT_OVER_THAN_LIMIT_PRICE);
		} catch (OrderValidationException e){
			throw new OrderValidationException(e.getMessage(),e.getClientMessage());
		} catch(Exception e) {
			throw new OrderValidationException("Field " + field + " has caused exception: " + e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}

	}

}
