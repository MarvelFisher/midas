package com.cyanspring.common.staticdata;

public enum RefDataCommodity {

	STOCK("S"), INDEX("I"), FUTUREINDEX("FI"), FUTURECOMMODITY("FC"),FOREX("FX");
	
	private String value;
	
	private RefDataCommodity(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
