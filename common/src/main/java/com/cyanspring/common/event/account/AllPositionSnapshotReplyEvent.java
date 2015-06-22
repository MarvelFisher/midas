package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllPositionSnapshotReplyEvent extends RemoteAsyncEvent{

	private List<OpenPosition> openPositionList;
	
	public AllPositionSnapshotReplyEvent(String key, String receiver,List<OpenPosition> ops) {
		super(key, receiver);
		openPositionList = ops;
	}

	public List<OpenPosition> getOpenPositionList() {
		return openPositionList;
	}
}
