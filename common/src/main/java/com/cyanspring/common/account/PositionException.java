package com.cyanspring.common.account;

import com.cyanspring.common.message.ErrorMessage;

public class PositionException extends Exception {
	private ErrorMessage clientMessage;

	public PositionException(String message) {
		super(message);
	}
	public PositionException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
