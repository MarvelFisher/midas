package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllPositionSnapshotRequestEvent extends RemoteAsyncEvent{

	public AllPositionSnapshotRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
