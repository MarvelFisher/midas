package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import webcurve.util.PriceUtils;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;

public class CreditPartialValidator implements IOrderValidator {
	private static final Logger log = LoggerFactory
			.getLogger(CreditPartialValidator.class);
	
	@Autowired(required=false)
	private AccountKeeper accountKeeper;
	
	@Autowired(required=false)
	private PositionKeeper positionKeeper;
	
	@Autowired
	IRefDataManager refDataManager;

	private IRefDataChecker refDataChecker;
	private double ratio = 0.2;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if(null == accountKeeper || null == positionKeeper)
			return;

		try {
			if(null == order) { // new order
				String symbol = (String)map.get(OrderField.SYMBOL.value());
				RefData refData = refDataManager.getRefData(symbol);
				if (refData == null)
					throw new OrderValidationException("Can't find refData", ErrorMessage.REF_SYMBOL_NOT_FOUND);
				
				if(!refDataChecker.check(refData)) // no null check here because refDataChecker must be set or its a programming error!!!
					return;
				
				Quote quote = positionKeeper.getQuote(symbol);
				if(null == quote) {
					log.warn("No quote for " + symbol + " can't validate credit");
					return;
				}
				
				String accountId = (String)map.get(OrderField.ACCOUNT.value());
				Account account = accountKeeper.getAccount(accountId);
				OrderSide side = (OrderSide)map.get(OrderField.SIDE.value());
				double qty = (Double)map.get(OrderField.QUANTITY.value());
				if(!positionKeeper.checkPartialCreditByAccountAndSymbol(account, symbol, quote, 
						side.isBuy()?qty:-qty, ratio))
					throw new OrderValidationException("Order execeeds account value percentage of " + ratio, ErrorMessage.ORDER_OVER_ACCOUNT_VALUE_PERCENTAGE);
			} else { //amemnd order
				RefData refData = refDataManager.getRefData(order.getSymbol());
				if (refData == null)
					throw new OrderValidationException("Can't find refData", ErrorMessage.REF_SYMBOL_NOT_FOUND);
				
				if(!refDataChecker.check(refData)) // no null check here because refDataChecker must be set or its a programming error!!!
					return;

				Quote quote = positionKeeper.getQuote(order.getSymbol());
				if(null == quote) {
					log.warn("No quote for " + order.getSymbol() + " can't validate credit");
					return;
				}
				
				Account account = accountKeeper.getAccount(order.getAccount());
				Object oQty = map.get(OrderField.QUANTITY.value());
				if(null == oQty) // not changing quantity
					return;
				
				double qty = (Double)oQty;
				
				if(PriceUtils.EqualLessThan(qty, order.getQuantity())) // amending down quantity no risk
					return;
				
				qty -= order.getQuantity();
				
				if(!positionKeeper.checkPartialCreditByAccountAndSymbol(account, order.getSymbol(), quote, 
						order.getSide().isBuy()?qty:-qty, ratio))
					throw new OrderValidationException("Order execeeds account value percentage of " + ratio, ErrorMessage.ORDER_OVER_ACCOUNT_VALUE_PERCENTAGE);
			}
		} catch(OrderValidationException e) {
			throw e;
		} catch(Exception e) {
			throw new OrderValidationException(e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}
	}

	public IRefDataChecker getRefDataChecker() {
		return refDataChecker;
	}

	public void setRefDataChecker(IRefDataChecker refDataChecker) {
		this.refDataChecker = refDataChecker;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	
}
