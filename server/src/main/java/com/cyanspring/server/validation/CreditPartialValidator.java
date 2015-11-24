package com.cyanspring.server.validation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.position.PositionKeeper;
import com.cyanspring.common.staticdata.IRefDataChecker;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

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
				if(!positionKeeper.checkPartialCreditByAccountAndRefDataChecker(account, symbol, quote, 
						Default.getCreditPartial(), 
						refDataChecker,
						side.isBuy(), 0.0, 0.0, qty, price))
					throw new OrderValidationException("Order execeeds account value percentage of " + Default.getCreditPartial(), ErrorMessage.ORDER_OVER_ACCOUNT_VALUE_PERCENTAGE);

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
				double newPrice = null == oPrice? oldPrice : (double)oQty;
				if(order.getOrderType().equals(OrderType.Market)) {
					oldPrice = newPrice = QuoteUtils.getRiskPrice(order.getOrderType(), order.getSide(), oldPrice, quote);
				}

				if(!positionKeeper.checkPartialCreditByAccountAndRefDataChecker(account, order.getSymbol(), quote,
						Default.getCreditPartial(), 
						refDataChecker,
						order.getSide().isBuy(), oldQty, oldPrice, newQty, newPrice))
					throw new OrderValidationException("Order execeeds account value percentage of " + Default.getCreditPartial(), ErrorMessage.ORDER_OVER_ACCOUNT_VALUE_PERCENTAGE);
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
}
