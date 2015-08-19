package com.cyanspring.common.filter;

import com.cyanspring.common.message.ErrorMessage;

public class DataObjectFilterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3659011448007604975L;
	private ErrorMessage clientMessage;

	public DataObjectFilterException(String message) {
		super(message);
	}
	public DataObjectFilterException(String localMessage,ErrorMessage clientMessage) {
		super(localMessage);
		this.clientMessage = clientMessage;

	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}

}
