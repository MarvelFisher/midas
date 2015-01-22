package com.cyanspring.common.event.alert;

public enum AlertType {
	PRICE_SET_NEW(1),
	PRICE_SET_MODIFY(2),
	PRICE_SET_CANCEL(3),
	PRICE_QUERY_CUR(4),
	PRICE_QUERY_PAST(5),
	TRADE_QUERY_OLD(6),
	;
	
	private int value;
	AlertType(int value) {
		this.value = value;
	}
	public int value() {
		return value;
	}
}
