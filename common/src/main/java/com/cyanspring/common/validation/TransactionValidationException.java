package com.cyanspring.common.validation;

import com.cyanspring.common.message.ErrorMessage;

public class TransactionValidationException extends Exception {
	private static final long serialVersionUID = -3560842672907062890L;
	private ErrorMessage clientMessage;

	public TransactionValidationException(String message) {
		super(message);
	}
	public TransactionValidationException(String localMessage,ErrorMessage clientMessage) {
		super(localMessage);
		this.clientMessage = clientMessage;

	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}

}
