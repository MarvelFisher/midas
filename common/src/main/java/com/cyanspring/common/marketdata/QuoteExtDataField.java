package com.cyanspring.common.marketdata;

import java.util.HashMap;

public enum QuoteExtDataField {
	ID("id"),
	SYMBOL("Symbol"),
	CEIL("Ceil"),
	FLOOR("Floor"),
	SETTLEPRICE("SettlePrice"),
	OPENINTEREST("OpenInterest"),
	SESSIONSTATUS("SessionStatus"),
	PRECLOSE("PreClose"),
	STATUS("Status"),
	TIMESTAMP("TimeStamp"),
	TIMESENT("TimeSent"),
	BUYVOL("BuyVol"),
	SELLVOL("SellVol"),
	UNCLASSIFIEDVOL("UnclassifiedVol"),
	BUYTURNOVER("BuyTurnover"),
	SELLTURNOVER("SellTurnover"),
	UNCLASSIFIEDTURNOVER("UnclassifiedTurnover"),
	FREESHARES("FreeShares"),
	TOTOALSHARES("TotalShares"),
	PERATIO("PERatio"),
	CNNAME("CNName")
	;

	static HashMap<String, QuoteExtDataField> map = new HashMap<String, QuoteExtDataField>();
	
	private String value;
	QuoteExtDataField(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public QuoteExtDataField getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (QuoteExtDataField field: QuoteExtDataField.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("QuoteExtDataField duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}
	
}
