package com.cyanspring.common.event.account;

import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CSTWUserLoginReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private boolean isOk;
	private String message;
	private UserGroup userGroup;
	public CSTWUserLoginReplyEvent(String key, String receiver,boolean isOk,String message,UserGroup userGroup) {
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.userGroup = userGroup;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMessage() {
		return message;
	}
	public UserGroup getUserGroup() {
		return userGroup;
	}
}
