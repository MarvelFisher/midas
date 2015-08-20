package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

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
