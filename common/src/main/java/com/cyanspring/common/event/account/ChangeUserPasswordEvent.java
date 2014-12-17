package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeUserPasswordEvent extends RemoteAsyncEvent {
	private String user;
	private String originalPass;
	private String newPass;
	private String txId;

	public ChangeUserPasswordEvent(String key, String receiver, String user, String originalPass, String newPass, String txId) 
	{
		super(key, receiver);
		this.user = user;
		this.originalPass = originalPass;
		this.newPass = newPass;
		this.txId = txId;
	}
	public String getUser() 
	{
		return user;
	}
	public String getOriginalPassword()
	{
		return originalPass;
	}
	public String getNewPassword()
	{
		return newPass;
	}
	public String getTxId()
	{
		return txId;
	}
}