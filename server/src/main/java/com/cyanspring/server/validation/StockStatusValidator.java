package com.cyanspring.server.validation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.business.StockStatus;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.type.OrderAction;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.validation.data.IQuoteExtProvider;

public class StockStatusValidator implements IOrderValidator{
	

	private static final Logger log = LoggerFactory
			.getLogger(StockStatusValidator.class);
	
	@Autowired
	IQuoteExtProvider validationDataProvider;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		if( null == validationDataProvider ){
			log.info("validationDataProvider is null");
			return;
		}
		
		ConcurrentHashMap<String, DataObject> quoteExtendsMap = validationDataProvider.getQuoteExtMap();
		
		if(null == quoteExtendsMap || quoteExtendsMap.size()==0){					
			log.warn("validation : No quoteExtends info");
			return;		
		}
		
		try{
			
			String symbol = null;
			DataObject data = null;
			
			if(order == null){				
				symbol = (String)map.get(OrderField.SYMBOL.value());
			}else{			
				symbol = order.getSymbol();
			}
			
			data = quoteExtendsMap.get(symbol);
			
			if(null == data){
				log.warn("validation : QuoteExtend symbol not exist - "+symbol);
			}else{	
				
				Integer status = data.get(Integer.class, QuoteExtDataField.STATUS.value());
				
				if( null == status )
					 return;
				 
				if(StockStatus.STOP_SYMBOL.code() == status 
						||	StockStatus.STOP_SYMBOL_2.code() == status 
						||  StockStatus.PENDING.code() == status 
						||  StockStatus.PENDING_2.code() == status ){
					
					throw new OrderValidationException("Trading suspension!",ErrorMessage.TRADING_SUSPENSION);
				}
			}
		}catch(OrderValidationException e){	
			throw e;
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}	
	}

}
