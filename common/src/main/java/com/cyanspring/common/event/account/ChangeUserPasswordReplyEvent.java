package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeUserPasswordReplyEvent extends RemoteAsyncEvent {
	private String user;
	private boolean ok;
	private String message;
	private String txId;

	public ChangeUserPasswordReplyEvent(String key, String receiver, String user, boolean ok, String message, String txId) 
	{
		super(key, receiver);
		this.user = user;
		this.ok = ok;
		this.message = message;
		this.txId = txId;
	}
	public String getUser() 
	{
		return user;
	}
	public boolean isOk()
	{
		return ok;
	}
	public String getMessage()
	{
		return message;
	}
	public String getTxId()
	{
		return txId;
	}
}