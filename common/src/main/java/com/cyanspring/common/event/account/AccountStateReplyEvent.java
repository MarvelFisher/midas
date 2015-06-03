package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountStateReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String accountId;
	private String userId;
	private AccountState state;
	private boolean ok ;
	private String msg ;
	
	public AccountStateReplyEvent(String key, String receiver,boolean ok,String msg,String accountId,String userId,AccountState state) {
		
		super(key, receiver);	
		this.accountId = accountId;
		this.userId = userId;
		this.state = state;
		this.ok = ok;
		this.msg = msg;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getUserId() {
		return userId;
	}

	public AccountState getState() {
		return state;
	}

	public boolean isOk() {
		return ok;
	}

	public String getMsg() {
		return msg;
	}

}
