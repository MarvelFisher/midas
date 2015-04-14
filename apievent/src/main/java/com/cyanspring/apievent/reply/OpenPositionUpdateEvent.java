package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.ClientEvent;
import com.cyanspring.apievent.obj.OpenPosition;

public class OpenPositionUpdateEvent extends ClientEvent {
	private OpenPosition position;

	public OpenPositionUpdateEvent(String key, String receiver, OpenPosition position) {
		super(key, receiver);
		this.position = position;
	}

	public OpenPosition getPosition() {
		return position;
	}
	
}
