package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountClosedPositionSnapshotRequestEvent extends RemoteAsyncEvent{

	private String accountId;
	private String txId;
	
	public AccountClosedPositionSnapshotRequestEvent(String key, String receiver,String accountId,String txId) {
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
