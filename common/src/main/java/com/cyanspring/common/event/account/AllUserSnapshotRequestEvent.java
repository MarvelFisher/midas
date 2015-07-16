package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllUserSnapshotRequestEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;

	public AllUserSnapshotRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
