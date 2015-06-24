package com.cyanspring.common.event.statistic;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountNumberReplyEvent extends RemoteAsyncEvent{
	private static final long serialVersionUID = 1L;
	private int accountNumber;
	private boolean isOk;
	private String errorMessage;
	public AccountNumberReplyEvent(String key, String receiver,int accountNumber,boolean isOk ,String errorMessage) {
		super(key, receiver);
		this.accountNumber = accountNumber;
		this.isOk = isOk;
		this.errorMessage = errorMessage;
	}
	public long getAccountNumber() {
		return accountNumber;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
}
