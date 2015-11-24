package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.position.PositionKeeper;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

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
				String accountId = (String)map.get(OrderField.ACCOUNT.value());
				Account account = accountKeeper.getAccount(accountId);
				OrderSide side = (OrderSide)map.get(OrderField.SIDE.value());
				double qty = (Double)map.get(OrderField.QUANTITY.value());
				OrderType orderType = (OrderType)map.get(OrderField.TYPE.value());
				Quote quote = positionKeeper.getQuote(symbol);
				if(orderType.equals(OrderType.Market) && (null == quote || !QuoteUtils.validQuote(quote)))
					throw new OrderValidationException("Can't validate market order risk without valid quote", ErrorMessage.NO_QUOTE_DATA);
				
				double price = orderType.equals(OrderType.Market)? 0.0 : ((double)map.get(OrderField.PRICE.value()));
				price = QuoteUtils.getRiskPrice(orderType, side, price, quote);
				if(!positionKeeper.checkMarginDeltaByAccountAndSymbol(account, symbol, quote, 
						side.isBuy(), 0.0, 0.0, qty, price))
					throw new OrderValidationException("Order exceeds cash available", ErrorMessage.ORDER_ACCOUNT_OVER_CREDIT_LIMIT);
			} else { //amemnd order
				Quote quote = positionKeeper.getQuote(order.getSymbol());
				if(order.getOrderType().equals(OrderType.Market) && (null == quote || !QuoteUtils.validQuote(quote)))
					throw new OrderValidationException("Can't validate market order risk without valid quote", ErrorMessage.NO_QUOTE_DATA);
				
				Account account = accountKeeper.getAccount(order.getAccount());
				
				double oldQty = order.getQuantity();
				double oldPrice = order.getPrice();
				Object oQty = map.get(OrderField.QUANTITY.value());
				double newQty = null == oQty? order.getQuantity() : (double)oQty;
				Object oPrice = map.get(OrderField.PRICE.value());
				double newPrice = null == oPrice? oldPrice : (double)oPrice;
				if(order.getOrderType().equals(OrderType.Market)) {
					oldPrice = newPrice = QuoteUtils.getRiskPrice(order.getOrderType(), order.getSide(), oldPrice, quote);
				}

				if(!positionKeeper.checkMarginDeltaByAccountAndSymbol(account, order.getSymbol(), quote, 
						order.getSide().isBuy(), oldQty, oldPrice, newQty, newPrice))
					throw new OrderValidationException("Order exceeds cash available", ErrorMessage.AMEND_ORDER_OVER_CREDIT_LIMIT);
			}
		} catch(OrderValidationException e) {
			throw e;
		} catch(Exception e) {
			throw new OrderValidationException(e.getMessage(),ErrorMessage.VALIDATION_ERROR);
		}
	}

}
