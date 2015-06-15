package com.cyanspring.common.message;

public enum ExtraEventMessage {
	
	USER_STOP_LIVE_TRADING_START_TIME,
	USER_STOP_LIVE_TRADING_END_TIME;
	
	private String message;
	public void putMessage(String message){
		this.message = message;
	}
	public String getMessage(){
		return message;
	}
}
