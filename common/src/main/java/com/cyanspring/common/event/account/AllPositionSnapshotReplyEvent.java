package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllPositionSnapshotReplyEvent extends RemoteAsyncEvent{

	private List<OpenPosition> openPositionList;
	private List<ClosedPosition> closedPositionList;
	
	public AllPositionSnapshotReplyEvent(String key, String receiver,List<OpenPosition> ops,List<ClosedPosition> cps) {
		super(key, receiver);
		openPositionList = ops;
		closedPositionList = cps;
	}

	public List<OpenPosition> getOpenPositionList() {
		return openPositionList;
	}

	public List<ClosedPosition> getClosedPositionList() {
		return closedPositionList;
	}
}
