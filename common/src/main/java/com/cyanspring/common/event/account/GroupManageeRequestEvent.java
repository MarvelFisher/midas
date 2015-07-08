package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class GroupManageeRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String manager;
	public GroupManageeRequestEvent(String key, String receiver,String manager) {
		super(key, receiver);
		this.manager = manager;
	}
	
	public String getManager() {
		return manager;
	}
}
