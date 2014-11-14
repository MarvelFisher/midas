package com.cyanspring.common.event.signal;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CancelSignalEvent extends RemoteAsyncEvent {

	public CancelSignalEvent(String key, String receiver) {
		super(key, receiver);
	}

}
