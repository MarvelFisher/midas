package com.cyanspring.common.account;

import com.cyanspring.common.message.ErrorMessage;

public class UserException extends Exception {
	private ErrorMessage clientMessage;
	public UserException(String message) {
		super(message);
	}
	public UserException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
