package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CSTWUserLoginReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private boolean isOk;
	private String message;
	private UserGroup userGroup;
	private List<Account>accountList;
	public CSTWUserLoginReplyEvent(String key, String receiver,boolean isOk,String message,UserGroup userGroup,List<Account> accountList) {
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.userGroup = userGroup;
		this.accountList = accountList; 
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
	public List<Account> getAccountList() {
		return accountList;
	}
}
