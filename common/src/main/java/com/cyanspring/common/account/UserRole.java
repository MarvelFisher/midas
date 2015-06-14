package com.cyanspring.common.account;

public enum UserRole {
	Trader(1),
	RiskManager(2),
	Admin(3),
	;

	private final int value;
	private UserRole(int value) {
		this.value = value;
	}
	
	public int value() {
		return this.value;
	}

}
