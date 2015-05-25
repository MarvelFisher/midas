package com.cyanspring.common.event.alert;

public enum AlertType {
	PRICE_SET_NEW(1),
	PRICE_SET_MODIFY(2),
	PRICE_SET_CANCEL(3),
	PRICE_QUERY_CUR(4),
	PRICE_QUERY_PAST(5),
	TRADE_QUERY_PAST(6),
	PERCENTAGE_SET_NEW(7),
	PERCENTAGE_SET_MODIFY(8),
	PERCENTAGE_SET_CANCEL(9),
	PERCENTAGE_QUERY_CUR(10),
	PERCENTAGE_QUERY_PAST(11),
	;
	
	private int value;
	AlertType(int value) {
		this.value = value;
	}
	public int value() {
		return value;
	}
}
