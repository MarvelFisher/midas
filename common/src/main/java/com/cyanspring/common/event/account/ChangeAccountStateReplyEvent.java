package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ChangeAccountStateReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private boolean isOk ;
	private String message;
	private Account account;
	public ChangeAccountStateReplyEvent(String key, String receiver,boolean isOk,String message,Account account) {
		super(key, receiver);
		this.isOk = isOk;
		this.message = message;
		this.account = account;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMessage() {
		return message;
	}
	public Account getAccount() {
		return account;
	}
}
