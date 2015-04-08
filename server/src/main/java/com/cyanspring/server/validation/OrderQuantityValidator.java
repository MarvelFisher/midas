/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.validation.OrderValidationException;

public class OrderQuantityValidator implements IFieldValidator {
	@Autowired
	IRefDataManager refDataManager;

	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		try {
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
			
			if(qty.longValue() % refData.getLotSize() != 0)
				throw new OrderValidationException("Invalid Quantity! Quantity should be the multiple of 1000.",ErrorMessage.INVALID_QUANTITY);
//				throw new OrderValidationException(field + " not in round lot of " + refData.getLotSize() +": " + qty.longValue());
		} catch (OrderValidationException e){
			throw new OrderValidationException(e.getMessage(),e.getClientMessage());
		} catch (Exception e) {
			throw new OrderValidationException(field + " has caused exception: " + e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}
	}

}
