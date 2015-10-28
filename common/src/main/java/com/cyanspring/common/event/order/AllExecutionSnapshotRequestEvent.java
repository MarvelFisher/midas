package com.cyanspring.common.event.order;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllExecutionSnapshotRequestEvent extends RemoteAsyncEvent{
	
	private List<String> accountIdList;
	
	public AllExecutionSnapshotRequestEvent(String key, String receiver,List<String> accountIdList) {
		super(key, receiver);
		this.accountIdList = accountIdList;
	}

	public List<String> getAccountIdList() {
		return accountIdList;
	}
}
