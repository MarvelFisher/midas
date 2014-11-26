package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllAccountSnapshotReplyEvent extends RemoteAsyncEvent {
	private List<Account> accounts = new ArrayList<Account>();

	public AllAccountSnapshotReplyEvent(String key, String receiver,
			List<Account> accounts) {
		super(key, receiver);
		this.accounts = accounts;
	}

	public List<Account> getAccounts() {
		return accounts;
	}
	
}
