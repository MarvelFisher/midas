package com.cyanspring.common.marketdata;

import com.cyanspring.common.message.ErrorMessage;

public class TickDataException extends Exception {
	private static final long serialVersionUID = -139248299932144021L;
	private ErrorMessage clientMessage;
	public TickDataException(String message) {
		super(message);
	}
	public TickDataException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
