package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class InternalSessionRequestEvent extends RemoteAsyncEvent {

	public InternalSessionRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
