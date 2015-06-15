package com.cyanspring.common.event.statistic;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AccountStatisticRequestEvent extends RemoteAsyncEvent{

	public AccountStatisticRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
