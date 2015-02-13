package com.cyanspring.common.business;

import java.util.HashMap;

public enum RefDataField {
	SYMBOL("Symbol"),
	DESC("Desc"),
	EXCHANGE("Exchange"),
	CURRENCY("Currency"),
	FX_CURRENCY("FX Currency"),
	LOT_SIZE("Lot size"),
	OPEN("Open"),
	CLOSE("Close"),
	HIGH("High"),
	LOW("Low"),
	CONTRACT("Contract"),
	SINGLE_MA("single MA"),
	SHORT_MA("short MA"),
	MID_MA("mid MA"),
	LONG_MA("long MA"),
	EN_DISPLAYNAME("ENName"),
	TW_DISPLAYNAME("TWName"),
	CN_DISPLAYNAME("CNName"),
	REF_SYMBOL("Ref symbol"),
	TYPE("Type"), 
	CATEGORY("Category"),
	TICK_TABLE("Tick table"),
	SETTLEMENT_DATE("Settlement date"),  
	PRICE_PER_UNIT("Price per unit"),
	EN_TRADING_UNIT("ENTrading unit"),   
	TW_TRADING_UNIT("TWTrading unit"),   
	CN_TRADING_UNIT("CNTrading unit"),   
	MARGIN_RATE("Margin Rate"),          
	COMMISSION_FEE("Commission fee"),    
	MINIMAL_COMMISSION_FEE("Minimal CF"),
	MAXIMAL_LOT("Maximal lot"),     
	PRICE_LIMIT("Price limit"),     
	DECIMALPOINT("Decimal point"),  
	DENOMINATOR("Denominator"),     
	NUMERATOR_DP("Nnumerator DP"),
	TRADABLE("Tradable") 
	;
	
	static HashMap<String, RefDataField> map = new HashMap<String, RefDataField>();
	
	private String value;
	RefDataField(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public RefDataField getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (RefDataField field: RefDataField.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("RefDataField duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}


}
