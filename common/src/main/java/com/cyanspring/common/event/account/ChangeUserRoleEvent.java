package com.cyanspring.common.event.account;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeUserRoleEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String id;
	private UserRole role;
	public ChangeUserRoleEvent(String key, String receiver,String id,UserRole role) {
		super(key, receiver);
		this.id = id;
		this.role = role;
	}

	public String getId() {
		return id;
	}

	public UserRole getRole() {
		return role;
	}
    
}
