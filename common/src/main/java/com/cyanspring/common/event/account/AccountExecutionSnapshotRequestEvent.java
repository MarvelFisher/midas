package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountExecutionSnapshotRequestEvent extends RemoteAsyncEvent{

	private String accountId;
	private String txId;
	
	public AccountExecutionSnapshotRequestEvent(String key, String receiver,String accountId,String txId) {
		super(key, receiver);
		
	}

	public String getAccountId() {
		return accountId;
	}

	public String getTxId() {
		return txId;
	}
}
