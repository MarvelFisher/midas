package com.cyanspring.common.signal;

import java.util.HashMap;

public enum SignalField {
//	SYMBOL("Symbol"),
	DESC("Desc"),
	BID("Bid"),
	ASK("Ask"),
	BID_VOL("Bid vol"),
	ASK_VOL("Ask vol"),
	PREV_LAST("P. last"),
	LAST("Last"),	 
	LAST_VOL("Last vol"),
	VOL("Vol"), //total market volume
	OPEN("Open"),
	CLOSE("Close"),
	HIGH("High"),
	LOW("Low"),
	BUY_DRIVEN_VOL("BD Vol"),
	SELL_DRIVEN_VOL("SD Vol"),
	BSD("BSD"),
	HISTORICAL_BSD("H_BSD"),
	BSD_DATE("BSD Date"),
	BSD_DAYS("BSD days"),
	BSD_DELTA("BSD delta"),
	NOTE("Note"),
	CHANGE("Chg"),
	CHANGE_PERCENT("Chg%"),
	;
	
	static HashMap<String, SignalField> map = new HashMap<String, SignalField>();
	
	private String value;
	SignalField(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public SignalField getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (SignalField field: SignalField.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("SignalField duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}

}
