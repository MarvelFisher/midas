package com.cyanspring.common.fx;

import com.cyanspring.common.message.ErrorMessage;

public class FxException extends Exception {
	private ErrorMessage clientMessage;

	public FxException(String message) {
		super(message);
	}
	public FxException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
