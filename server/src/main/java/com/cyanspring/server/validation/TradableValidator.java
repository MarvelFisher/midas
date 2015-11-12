package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataBitUtil;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class TradableValidator implements IOrderValidator{
	
	private static final Logger log = LoggerFactory
			.getLogger(TradableValidator.class);
	
	@Autowired
	IRefDataManager refDataManager;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {

		String symbol = null;
		
		if( null == order ){
			symbol = (String)map.get(OrderField.SYMBOL.value());
		}else{
			symbol = order.getSymbol();
		}
		 
		if(!StringUtils.hasText(symbol))
			return;

		RefData refData = refDataManager.getRefData(symbol);

		if( null == refData ){
			log.warn("This symbol doesn't exist in refData :"+symbol);
			return;
		}

		if(RefDataBitUtil.isTradable(refData.getInstrumentType())){
			throw new OrderValidationException("Trade-related functions are not available for this contract."
					,ErrorMessage.SYMBOL_NOT_TRADABLE);
		}
	}

}
