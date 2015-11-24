package com.cyanspring.common.event.account;

import com.cyanspring.common.event.AbstractRequestEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;

public class MaxOrderQtyAllowedRequestEvent extends AbstractRequestEvent {
	private static final long serialVersionUID = 1L;
	
	private String account;
	private String symbol;
	private OrderSide orderSide;
	private OrderType orderType;
	private double price;
	
	public MaxOrderQtyAllowedRequestEvent(String key, String receiver,
			String txId, String account, String symbol, OrderSide orderSide,
			OrderType orderType, double price) {
		super(key, receiver, txId);
		this.account = account;
		this.symbol = symbol;
		this.orderSide = orderSide;
		this.orderType = orderType;
		this.price = price;
	}

	public String getAccount() {
		return account;
	}

	public String getSymbol() {
		return symbol;
	}

	public OrderSide getOrderSide() {
		return orderSide;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public double getPrice() {
		return price;
	}
	
	@Override
	public String toString() {
		return "MaxOrderQtyAllowedRequestEvent: " + 
				account + ", " +
				symbol + ", " +
				orderSide + ", " +
				orderType + ", " +
				price + ", " + this.getTxId();
	}
}
