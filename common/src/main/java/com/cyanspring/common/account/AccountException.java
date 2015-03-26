package com.cyanspring.common.account;

import com.cyanspring.common.message.ErrorMessage;

public class AccountException extends Exception {
	private ErrorMessage clientMessage;
	public AccountException(String message) {
		super(message);
	}
	public AccountException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}

}
