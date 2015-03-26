package com.cyanspring.server.persistence;

import com.cyanspring.common.message.ErrorMessage;

public class CentralDbException extends Exception 
{
	private ErrorMessage clientMessage;
	public CentralDbException(String message) 
	{
		super(message);
	}
	public CentralDbException(String localMessage,ErrorMessage clientMessage) {
		
		super(localMessage);
		this.clientMessage = clientMessage;
		
	}
	public ErrorMessage getClientMessage() {
		return clientMessage;
	}
}
