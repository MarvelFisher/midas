package com.cyanspring.common.staticdata.fu;

public enum IType {
	FUTURES_IDX("112"),
	FUTURES("113"),
	FUTURES_EFP("117"),
	FUTURES_CX_IDX("120"),
	FUTURES_CX("122")
	;
	private String value;
	private IType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getIntValue(){
		return Integer.parseInt(getValue());
	}
	
}
