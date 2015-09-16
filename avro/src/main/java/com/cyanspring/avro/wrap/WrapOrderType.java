package com.cyanspring.avro.wrap;

import com.cyanspring.avro.types.OrderType;

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

}
