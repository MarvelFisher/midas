package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ResetAccountRequestEvent extends RemoteAsyncEvent {
	private String account;
	private String txId;
	public ResetAccountRequestEvent(String key, String receiver,
			String account, String txId) {
		super(key, receiver);
		this.account = account;
		this.txId = txId;
	}
	
	public String getAccount() {
		return account;
	}
	public String getTxId() {
		return txId;
	}
	
}
