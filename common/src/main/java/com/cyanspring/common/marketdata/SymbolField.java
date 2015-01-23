package com.cyanspring.common.marketdata;

public enum SymbolField {
	symbolId(String.class), 
	market(String.class),
	cnName(String.class),
	enName(String.class);
	
	private SymbolField(Object obj) {
		this.obj = obj;		
	}
	
	Object obj;

	public Object getObj() {
		return obj;
	}
}
