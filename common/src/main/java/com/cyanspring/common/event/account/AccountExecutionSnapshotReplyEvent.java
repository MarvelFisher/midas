package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountExecutionSnapshotReplyEvent extends RemoteAsyncEvent{

	private String accountId;
	private String txId;
	private List<Execution> executions;
	
	public AccountExecutionSnapshotReplyEvent(String key, String receiver,String accountId, String txId,List<Execution> executions) {
		super(key, receiver);
		this.accountId = accountId;
		this.txId = txId;
		this.executions = executions;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getTxId() {
		return txId;
	}

	public List<Execution> getExecutions() {
		return executions;
	}


}
