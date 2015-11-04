package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllPositionSnapshotRequestEvent extends RemoteAsyncEvent{
	public enum PositionType {
		OpenPosition,ClosedPosition,All
	};
	
	private PositionType type;
	public AllPositionSnapshotRequestEvent(String key, String receiver,PositionType type) {
		super(key, receiver);
		this.type = type;
	}
	
	public PositionType getType() {
		return type;
	}
}
