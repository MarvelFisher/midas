package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ActiveAccountRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String account;
	public ActiveAccountRequestEvent(String key, String receiver,String account) {
		super(key, receiver);
		this.account = account;
	}
	public String getAccount() {
		return account;
	}
}
