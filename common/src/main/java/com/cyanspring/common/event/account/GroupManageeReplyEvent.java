package com.cyanspring.common.event.account;

import java.util.Set;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class GroupManageeReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	public Set manageeSet;
	private boolean isOk;
	private String messsage;
	public GroupManageeReplyEvent(String key, String receiver,boolean isOk,String message,Set manageeSet) {
		super(key, receiver);
		this.manageeSet = manageeSet;
		this.isOk = isOk;
		this.messsage = message;
	}
	public Set getManageeSet() {
		return manageeSet;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMesssage() {
		return messsage;
	}
	 
}
