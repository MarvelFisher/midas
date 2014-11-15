package com.cyanspring.common.event.account;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClosedPositionUpdateEvent extends RemoteAsyncEvent {
	private ClosedPosition position;

	public ClosedPositionUpdateEvent(String key, String receiver,
			ClosedPosition position) {
		super(key, receiver);
		this.position = position;
	}

	public ClosedPosition getPosition() {
		return position;
	}
	
	
}
