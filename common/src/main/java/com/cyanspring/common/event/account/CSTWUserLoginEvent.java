package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CSTWUserLoginEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String id;
	private String password;
	public CSTWUserLoginEvent(String key, String receiver,String id,String password) {
		super(key, receiver);
		this.id = id;
		this.password = password;
	}
	public String getId() {
		return id;
	}
	public String getPassword() {
		return password;
	}
}
