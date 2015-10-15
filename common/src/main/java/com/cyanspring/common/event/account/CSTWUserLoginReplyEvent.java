package com.cyanspring.common.event.account;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.UserGroup;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CSTWUserLoginReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private boolean isOk;
	private String message;
	private UserGroup userGroup;
	private List<Account> accountList;
	private Map<String, Account> user2AccountMap;	// prepare first account of specified user
	public CSTWUserLoginReplyEvent(String key, String receiver,boolean isOk,String message,UserGroup userGroup,List<Account> accountList, Map<String, Account> user2AccountMap) {
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.userGroup = userGroup;
		this.accountList = accountList; 
		this.user2AccountMap = user2AccountMap;
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
	public Map<String, Account> getUser2AccountMap() {
		return user2AccountMap;
	}
}
