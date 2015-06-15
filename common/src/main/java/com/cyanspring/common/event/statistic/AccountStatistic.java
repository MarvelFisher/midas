package com.cyanspring.common.event.statistic;

import java.util.HashMap;

import com.cyanspring.common.marketdata.QuoteExtDataField;

public enum AccountStatistic {
	ACCOUNT_VALUE("Start Account Value"),
	VALUE("Value"),
	CASH_DEPOSITED("Cash Deposited"),
	PNL("P&L"),
	UR_PNL("Unrealized P&L"),
	DAILY_PNL("Daily P&L"),
	ALL_TIME_PNL("All Time P&L");
	
	static HashMap<String, AccountStatistic> map = new HashMap<String, AccountStatistic>();
	
	private String value;
	AccountStatistic(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public AccountStatistic getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (AccountStatistic field: AccountStatistic.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("AccountStatistic duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}
}
