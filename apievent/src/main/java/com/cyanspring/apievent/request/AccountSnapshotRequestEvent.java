package com.cyanspring.apievent.request;

import com.cyanspring.apievent.ClientEvent;

public class AccountSnapshotRequestEvent extends ClientEvent {
	private String accountId;
	private String txId;

	public AccountSnapshotRequestEvent(String key, String receiver,
									   String accountId,
									   String txId) {
		super(key, receiver);
		this.accountId = accountId;
		this.txId = txId;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getTxId() {
		return txId;
	}

	
}
