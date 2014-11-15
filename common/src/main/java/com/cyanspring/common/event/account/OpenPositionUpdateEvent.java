package com.cyanspring.common.event.account;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class OpenPositionUpdateEvent extends RemoteAsyncEvent {
	private OpenPosition position;

	public OpenPositionUpdateEvent(String key, String receiver, OpenPosition position) {
		super(key, receiver);
		this.position = position;
	}

	public OpenPosition getPosition() {
		return position;
	}
	
}
