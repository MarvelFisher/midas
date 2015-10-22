package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class OverAllPositionRequestEvent extends RemoteAsyncEvent {
	
	private List <String>accountIdList; 
	public OverAllPositionRequestEvent(String key, String receiver,List <String>accountIdList) {
		super(key, receiver);
		this.accountIdList = accountIdList;
	}
	
	public List<String> getAccountIdList() {
		return accountIdList;
	}
}
