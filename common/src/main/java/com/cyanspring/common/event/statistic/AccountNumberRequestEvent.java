package com.cyanspring.common.event.statistic;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountNumberRequestEvent extends RemoteAsyncEvent{
	private static final long serialVersionUID = 1L;
	public AccountNumberRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
