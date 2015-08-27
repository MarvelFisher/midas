package com.cyanspring.server.validation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.validation.data.IQuoteExtProvider;
/**
 * 
 * @author Jimmy
 * 
 * @Desc : prevent order price over than ceil or lower than floor price
 * 
 */
public class CeilFloorValidator implements IOrderValidator{
	
	private static final Logger log = LoggerFactory
			.getLogger(CeilFloorValidator.class);
		
	@Autowired
	public IQuoteExtProvider validationDataProvider;

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if( null == validationDataProvider ){
			return;
		}
		
		ConcurrentHashMap<String, DataObject> quoteExtendsMap = validationDataProvider.getQuoteExtMap();
		
		if(quoteExtendsMap.size()==0){					
			log.warn("validation : No quoteExtends info");
			return;		
		}

		try{
			
			String symbol;
			Double price;
			DataObject data = null;
			OrderType type = null;
			
			if(order == null){				
				symbol = (String)map.get(OrderField.SYMBOL.value());
				type =(OrderType) map.get(OrderField.TYPE.value());		
			}else{			
				symbol = order.getSymbol();
				type = order.getOrderType();	
			}
			
			if(OrderType.Market == type){			
				log.info("this is market order");
				return;			
			}
				
			if(null!=quoteExtendsMap && quoteExtendsMap.size()!=0){
				data = quoteExtendsMap.get(symbol);
			}
			 
			if(null == data){
				log.warn("validation : QuoteExtend symbol not exist - "+symbol);
			}else{
				
				price = (Double) map.get(OrderField.PRICE.value());
				if(null == price){					
					return;						
				}
								
				Double ceil = data.get(Double.class, QuoteExtDataField.CEIL.value());
				Double floor = data.get(Double.class, QuoteExtDataField.FLOOR.value());				
	
				if(ceil != null && PriceUtils.GreaterThan(price, ceil)){
					throw new OrderValidationException("Order price over than ceil price:"+ceil,ErrorMessage.ORDER_OVER_CEIL_PRICE);
				}
				
				if(floor != null && PriceUtils.LessThan(price,floor)){
					throw new OrderValidationException("Order price lower than floor price:"+floor,ErrorMessage.ORDER_LOWER_FLOOR_PRICE);
				}		
			}	
		}catch(OrderValidationException e){	
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}			
	}
}
