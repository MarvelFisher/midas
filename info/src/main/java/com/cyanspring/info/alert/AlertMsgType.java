package com.cyanspring.info.alert;

public enum AlertMsgType {
	MSG_TYPE_PRICE("1"),
	MSG_TYPE_ORDER("2"),
	MSG_TYPE_PREMIUMORDER("3")
	;
	
	private String Type;
	AlertMsgType(String alertMsgType)
	{
		this.Type = alertMsgType ;
	}
	
	public String getType()
	{
		return Type ;
	}	
}
