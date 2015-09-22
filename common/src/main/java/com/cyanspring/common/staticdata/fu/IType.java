package com.cyanspring.common.staticdata.fu;

public enum IType {
	FUTURES_FT("2"),
	FUTURES_IDX("112"),
	FUTURES("113"),
	FUTURES_EFP("117"),
	FUTURES_CX_IDX("120"),
	FUTURES_CX("122"),
	STOCK("16"),
	STOCK_SSE("17"),
	STOCK_GEI("18"),
	EXCHANGE_INDEX("1")
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
	public static boolean isIndex(String iType){
		
		if(iType.equals(EXCHANGE_INDEX.getValue()))
			return true;
		
		return false;
	}
	
	public static boolean isStock(String iType){
		if(iType.equals(STOCK.getValue())
			||iType.equals(STOCK_SSE.getValue())
			||iType.equals(STOCK_GEI.getValue())){
			return true;
		}
		return false;
	}
	
	public static boolean isFuture(String iType){
		if(iType.equals(FUTURES_IDX.getValue())
			||iType.equals(FUTURES.getValue())
			||iType.equals(FUTURES_EFP.getValue())
			||iType.equals(FUTURES_CX_IDX.getValue())
			||iType.equals(FUTURES_CX.getValue())){
			return true;
		}
		return false;
	}
	
}
