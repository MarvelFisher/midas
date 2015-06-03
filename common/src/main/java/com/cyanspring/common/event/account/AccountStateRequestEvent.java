package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountStateRequestEvent extends RemoteAsyncEvent{
	
	private static final long serialVersionUID = 1L;
	private String id;
	
	public AccountStateRequestEvent(String key, String receiver,String id) {

		super(key, receiver);	
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
