package com.cyanspring.common.staticdata.fu;

public enum IType {
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
	
	public static boolean isStock(String iType){
		if(iType.equals(STOCK)
			||iType.equals(STOCK_SSE)
			||iType.equals(STOCK_GEI)
			||iType.equals(EXCHANGE_INDEX)){
			return true;
		}
		return false;
	}
	
	public static boolean isFuture(String iType){
		if(iType.equals(FUTURES_IDX)
			||iType.equals(FUTURES)
			||iType.equals(FUTURES_EFP)
			||iType.equals(FUTURES_CX_IDX)
			||iType.equals(FUTURES_CX)){
			return true;
		}
		return false;
	}
	
}
