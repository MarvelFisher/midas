package com.cyanspring.avro.wrap;

import java.util.HashMap;

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

	private static HashMap<Integer, WrapOrderSide> map4Code;

	private static HashMap<com.cyanspring.common.type.OrderSide, WrapOrderSide> map4CommonOrderSide;

	static {
		WrapOrderSide[] values = values();
		map4Code = new HashMap<Integer, WrapOrderSide>(values.length);
		map4CommonOrderSide = new HashMap<com.cyanspring.common.type.OrderSide, WrapOrderSide>(
				values.length);
		for (WrapOrderSide wrapOrderSide : values) {
			map4Code.put(wrapOrderSide.code, wrapOrderSide);
			map4CommonOrderSide.put(wrapOrderSide.commonOrderSide,
					wrapOrderSide);
		}
	}

	public static WrapOrderSide valueOf(int code) {
		if (map4Code.containsKey(code)) {
			return map4Code.get(code);
		}
		return null;
	}

	public static WrapOrderSide valueOf(
			com.cyanspring.common.type.OrderSide commonOrderSide) {
		if (map4CommonOrderSide.containsKey(commonOrderSide)) {
			return map4CommonOrderSide.get(commonOrderSide);
		}
		return null;
	}

}
