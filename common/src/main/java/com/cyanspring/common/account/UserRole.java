package com.cyanspring.common.account;

public enum UserRole {
	Trader(1,"Trader"),
	RiskManager(2,"Frontend Risk Manager"),//FrontEndRiskManager
	Admin(3,"Admin"),
	BackEndRiskManager(4,"Backend Risk Manager"),
	;

	private final int value;
	private final String desc;
	private UserRole(int value,String desc) {
		this.value = value;
		this.desc = desc;
	}
	
	public  boolean isManagerLevel(){
		if(RiskManager == this || BackEndRiskManager == this){
			return true;
		}
		return false;
	}
	
	public int value() {
		return this.value;
	}
	
	public String desc() {
		return this.desc;
	}

}
