package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ActiveAccountReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String account;
	private boolean isOk;
	private String message;
	public ActiveAccountReplyEvent(String key, String receiver,String account,boolean isOk,String message) {
		super(key, receiver);
		this.account =account;
		this.isOk = isOk;
		this.message = message;
	}
	public String getAccount() {
		return account;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMessage() {
		return message;
	}
}
