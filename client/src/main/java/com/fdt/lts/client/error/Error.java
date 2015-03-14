package com.fdt.lts.client.error;

public enum Error {
	
	SERVER_ERROR(1, "Interal server error."),	
	SEND_ERROR(2, "Cannot send event to server"),
	LOGIN_ERROR(3, "Login fail."),
	INIT_ERROR(4, "Initial fail."),
	NEW_ORDER_ERROR(5, "New order fail"),
	AMEND_ORDER_ERROR(6, "Amend order fail"),
	CANCEL_ORDER_ERROR(7, "Cancel order fail");
	
	private Error(int code, String msg){
		this.code = code;
		this.msg = msg;
	}
	private int code;
	private String msg;
	
	public int getCode(){
		return code;
	}
	
	public String getMsg(){
		return msg;
	}
}
