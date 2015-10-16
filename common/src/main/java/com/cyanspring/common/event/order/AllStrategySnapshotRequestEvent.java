package com.cyanspring.common.event.order;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllStrategySnapshotRequestEvent extends RemoteAsyncEvent{
	
	private List <String>accountIdList;// if null get all
	public AllStrategySnapshotRequestEvent(String key, String receiver,List<String> accountIdList) {
		super(key, receiver);
		this.accountIdList = accountIdList;
	}
	public List <String>getAccountIdList() {
		return accountIdList;
	}
}
