package com.cyanspring.server.validation;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.OrderValidationException;

public class StockOrderQuantityValidator implements IFieldValidator{
	
	private static final Logger log = LoggerFactory
			.getLogger(StockOrderQuantityValidator.class);
	
	@Autowired
	private IRefDataManager refDataManager;	

	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		try {
			Iterator <String>ii = map.keySet().iterator();
			while(ii.hasNext()){
				String key = ii.next();
				log.info("key:{} value:{}",key,map.get(key));
			}
			
			Double qty = (Double)value;
			if(qty == null)
				throw new OrderValidationException(field + " can not be null",ErrorMessage.ORDER_FIELD_EMPTY);
			
			if(!PriceUtils.GreaterThan(qty, 0))
				throw new OrderValidationException(field + " must be greater than 0",ErrorMessage.ORDER_FIELD_MUST_GREATER_THAN_ZERO);
			
			if(!PriceUtils.Equal(qty, (double)qty.longValue()))
				throw new OrderValidationException(field + " must be an integer",ErrorMessage.ORDER_FIELD_MUST_BE_INTEGER);
			
			String symbol = (String)map.get(OrderField.SYMBOL.value());
			if(symbol == null)
				symbol = order.getSymbol();
			
			if(null == symbol)
				throw new OrderValidationException("Can not determine symbol for quantity lot size validation",ErrorMessage.ORDER_SYMBOL_LOT_SIZE_ERROR);
			
			RefData refData = refDataManager.getRefData(symbol);
			if(null == refData)
				throw new OrderValidationException("Can't find symbol in refdata: " + symbol,ErrorMessage.ORDER_SYMBOL_NOT_FOUND);
			
			OrderSide side = (OrderSide) map.get(OrderField.SIDE.value());
				
			if( side.isBuy() && qty.longValue() % refData.getLotSize() != 0)
				throw new OrderValidationException("Invalid Quantity!",ErrorMessage.INVALID_QUANTITY);
		
		} catch (OrderValidationException e){
			throw e;
		} catch (Exception e) {
			log.warn(e.getMessage(),e);
			throw new OrderValidationException(field + " has caused exception: " + e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}
	}
	
}
