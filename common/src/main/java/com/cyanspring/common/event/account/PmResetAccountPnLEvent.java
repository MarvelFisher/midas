package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmResetAccountPnLEvent extends RemoteAsyncEvent {

	public PmResetAccountPnLEvent(String key, String receiver) {
		super(key, receiver);
	}

}
