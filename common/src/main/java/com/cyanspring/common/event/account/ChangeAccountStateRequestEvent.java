package com.cyanspring.common.event.account;

import com.cyanspring.common.account.AccountState;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeAccountStateRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String id;
	private AccountState state;
	public ChangeAccountStateRequestEvent(String key, String receiver,String id,AccountState state) {
		super(key, receiver);
		this.id = id;
		this.state = state;
	}
	
	public String getId() {
		return id;
	}
	public AccountState getState() {
		return state;
	}
}
