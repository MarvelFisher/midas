package com.cyanspring.avro.wrap;

import java.util.HashMap;

import com.cyanspring.avro.generate.trade.types.OrderType;

public enum WrapOrderType {

	Market(0, OrderType.Market, com.cyanspring.common.type.OrderType.Market),

	Limit(1, OrderType.Limit, com.cyanspring.common.type.OrderType.Limit),

	;

	private int code;
	private OrderType orderType;
	private com.cyanspring.common.type.OrderType commonOrderType;

	private WrapOrderType(int code, OrderType orderType,
			com.cyanspring.common.type.OrderType commonOrderType) {
		this.code = code;
		this.orderType = orderType;
		this.commonOrderType = commonOrderType;
	}

	public int getCode() {
		return code;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public com.cyanspring.common.type.OrderType getCommonOrderType() {
		return commonOrderType;
	}
	
	private static HashMap<Integer, WrapOrderType> map4Code;

	private static HashMap<com.cyanspring.common.type.OrderType, WrapOrderType> map4CommonOrderType;

	static {
		WrapOrderType[] values = values();
		map4Code = new HashMap<Integer, WrapOrderType>(values.length);
		map4CommonOrderType = new HashMap<com.cyanspring.common.type.OrderType, WrapOrderType>(
				values.length);
		for (WrapOrderType wrapOrderType : values) {
			map4Code.put(wrapOrderType.code, wrapOrderType);
			map4CommonOrderType.put(wrapOrderType.commonOrderType,
					wrapOrderType);
		}
	}

	public static WrapOrderType valueOf(int code) {
		if (map4Code.containsKey(code)) {
			return map4Code.get(code);
		}
		return null;
	}

	public static WrapOrderType valueOf(com.cyanspring.common.type.OrderType commonOrderType) {
		if (map4CommonOrderType.containsKey(commonOrderType)) {
			return map4CommonOrderType.get(commonOrderType);
		}
		return null;
	}
	
	

}
