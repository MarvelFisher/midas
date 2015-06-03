package com.cyanspring.common.account;

import java.util.HashMap;
import java.util.Map;

public enum AccountState {
	ACTIVE(0),
	FROZEN(1),
	TERMINATED(2);

	private final int value;
	AccountState(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	static Map<Integer, AccountState> map = new HashMap<>();
	static {
		for (AccountState status : values()) {
			map.put(status.getValue(), status);
		}
	}

	public static AccountState fromInt(int value){
		return map.get(value);
	}
}
