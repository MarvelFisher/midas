package com.cyanspring.server.livetrading;

import com.cyanspring.common.message.ErrorMessage;

public class LiveTradingException extends Exception{
	private ErrorMessage clientMessage;

	public LiveTradingException(String message) {
		super(message);
	}
	
	public LiveTradingException(String localMessage,ErrorMessage clientMessage) {
		super(localMessage);
		this.clientMessage = clientMessage;
	}
	
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
	
}
