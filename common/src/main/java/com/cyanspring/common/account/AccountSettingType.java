package com.cyanspring.common.account;

import java.util.HashMap;

public enum AccountSettingType {
	ID("Id"),
	DEFAULT_QTY("Default Qty"),
	STOP_LOSS_VALUE("SL Value"),
	COMPANY_SL_VALUE("Company SL Value"),
	MARGIN("Margin"),
	ROUTE("Route"),
	LEVERAGE_RATE("Leverage Rate"),
	COMMISSION("Commission"),
	DAILY_STOPLOSS("daily Stop Loss"),
	TRAILING_STOP("Trailing stop"),
	STOP_LOSS_PERCENT("Stop loss percent"),
	FREEZE_PERCENT("Freeze percent"),
	TERMINATE_PERCENT("Terminate percent"),
	LIVE_TRADING("Live trading"),
	USER_LIVE_TRADING("User live trading"),
	LIVE_TRADING_TYPE("User live trading type")
	;
   
	static HashMap<String, AccountSettingType> map = new HashMap<String, AccountSettingType>();
	
	private String value;
	AccountSettingType(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public AccountSettingType getValue(String str) {
		return map.get(str);
	}

	static {
		map.clear();
		for (AccountSettingType field: AccountSettingType.values()) {
			if (map.containsKey(field.value())) {
				System.out.println("Error: duplicate AccountSettingType: " + field.value());
				System.exit(1);
			} else {
				map.put(field.value(), field);
			}
		}
	
	}
}
