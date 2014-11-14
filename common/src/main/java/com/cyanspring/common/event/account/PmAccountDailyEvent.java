package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmAccountDailyEvent extends RemoteAsyncEvent {

	public PmAccountDailyEvent(String key, String receiver) {
		super(key, receiver);
	}

}
