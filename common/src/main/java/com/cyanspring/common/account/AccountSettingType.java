package com.cyanspring.common.account;

import java.util.HashMap;

public enum AccountSettingType {
	ID("Id"),
	DEFAULT_QTY("Default Qty"),
	STOP_LOSS_VALUE("SL Value"),
	COMPANY_SL_VALUE("Company SL Value"), 
	ROUTE("Route"),
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
