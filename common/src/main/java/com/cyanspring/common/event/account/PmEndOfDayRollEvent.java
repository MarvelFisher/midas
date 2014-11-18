package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmEndOfDayRollEvent extends RemoteAsyncEvent {

	public PmEndOfDayRollEvent(String key, String receiver) {
		super(key, receiver);
	}

}
