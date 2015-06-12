package com.cyanspring.common.business;

public enum StockStatus {
	
	NOT_SETTING(0,"0"),
	NEW_PRODUCT(89,"Y"),
	STOP_SYMBOL(66,"B"),
	STOP_SYMBOL_2(68,"D"),
	PENDING(87,"W"),
	PENDING_2(88,"X");
	
	private final int code;
	private final String value;
	
	private StockStatus(int code ,String value) {
		this.value = value;
		this.code = code;
	}
	
	public String value() {
		return this.value;
	}
	
	public int code() {
		return this.code;
	}
}
