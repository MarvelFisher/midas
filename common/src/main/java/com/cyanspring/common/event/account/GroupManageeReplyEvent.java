package com.cyanspring.common.event.account;

import java.util.Set;

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class GroupManageeReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	public UserGroup userGroup;
	private boolean isOk;
	private String messsage;
	public GroupManageeReplyEvent(String key, String receiver,boolean isOk,String message,UserGroup userGroup) {
		super(key, receiver);
		this.userGroup = userGroup;
		this.isOk = isOk;
		this.messsage = message;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMesssage() {
		return messsage;
	}
	public UserGroup getUserGroup() {
		return userGroup;
	}
	 
}
