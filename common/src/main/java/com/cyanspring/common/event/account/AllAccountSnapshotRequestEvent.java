package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllAccountSnapshotRequestEvent extends RemoteAsyncEvent {

	public AllAccountSnapshotRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
