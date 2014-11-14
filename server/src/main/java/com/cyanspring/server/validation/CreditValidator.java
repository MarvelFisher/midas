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
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.server.account.AccountKeeper;
import com.cyanspring.server.account.PositionKeeper;

public class CreditValidator implements IOrderValidator {
	private static final Logger log = LoggerFactory
			.getLogger(CreditValidator.class);
	
	@Autowired(required=false)
	private AccountKeeper accountKeeper;
	
	@Autowired(required=false)
	private PositionKeeper positionKeeper;
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if(null == accountKeeper || null == positionKeeper)
			return;

		try {
			if(null == order) { // new order
				String symbol = (String)map.get(OrderField.SYMBOL.value());
				Quote quote = positionKeeper.getQuote(symbol);
				if(null == quote) {
					log.warn("No quote for " + symbol + " can't validate credit");
					return;
				}
				String accountId = (String)map.get(OrderField.ACCOUNT.value());
				Account account = accountKeeper.getAccount(accountId);
				OrderSide side = (OrderSide)map.get(OrderField.SIDE.value());
				double qty = (Double)map.get(OrderField.QUANTITY.value());
				if(!positionKeeper.checkMarginDeltaByAccountAndSymbol(account, symbol, quote, 
						side.isBuy()?qty:-qty))
					throw new OrderValidationException("This order would have caused account over credit limit");
			} else { //amemnd order
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
				
				if(!positionKeeper.checkMarginDeltaByAccountAndSymbol(account, order.getSymbol(), quote, 
						order.getSide().isBuy()?qty:-qty))
					throw new OrderValidationException("Amendment would have caused the account over credit limit");
			}
		} catch(OrderValidationException e) {
			throw e;
		} catch(Exception e) {
			throw new OrderValidationException(e.getMessage());
		}
	}

}
