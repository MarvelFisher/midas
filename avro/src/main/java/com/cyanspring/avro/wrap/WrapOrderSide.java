package com.cyanspring.avro.wrap;

import com.cyanspring.avro.types.OrderSide;

public enum WrapOrderSide {

	Buy(0, OrderSide.Buy, com.cyanspring.common.type.OrderSide.Buy),

	Sell(1, OrderSide.Sell, com.cyanspring.common.type.OrderSide.Sell),

	;

	private int code;
	private OrderSide orderSide;
	private com.cyanspring.common.type.OrderSide commonOrderSide;

	private WrapOrderSide(int code, OrderSide orderSide,
			com.cyanspring.common.type.OrderSide commonOrderSide) {
		this.code = code;
		this.orderSide = orderSide;
		this.commonOrderSide = commonOrderSide;
	}

	public int getCode() {
		return code;
	}

	public OrderSide getOrderSide() {
		return orderSide;
	}

	public com.cyanspring.common.type.OrderSide getCommonOrderSide() {
		return commonOrderSide;
	}

}
