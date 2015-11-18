package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountClosedPositionSnapshotReplyEvent extends RemoteAsyncEvent{
	
	private String accountId;
	private String txId;
	private List<ClosedPosition> closedPositions;

	public AccountClosedPositionSnapshotReplyEvent(String key, String receiver,String accountId,String txId,List<ClosedPosition> closedPositions) {
		super(key, receiver);
		this.accountId = accountId;
		this.txId = txId;
		this.closedPositions = closedPositions;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getTxId() {
		return txId;
	}

	public List<ClosedPosition> getClosedPositions() {
		return closedPositions;
	}

}
