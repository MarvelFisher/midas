package com.cyanspring.common.event.statistic;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountStatisticReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;

	private Map <String,Object> account;
	
	public AccountStatisticReplyEvent(String key, String receiver,Map <String,Object>account) {
		super(key, receiver);
		this.account =account;
	}

	public Map<String, Object> getAccount() {
		return account;
	}
}
