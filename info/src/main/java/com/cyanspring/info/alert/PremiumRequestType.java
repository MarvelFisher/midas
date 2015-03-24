package com.cyanspring.info.alert;

public enum PremiumRequestType {
	QUERY_All(1),
	QUERY_UPDATE(2),
	;
	
	private int Type; 
	PremiumRequestType (int type)
	{
		setType(type);
	}
	
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
}
