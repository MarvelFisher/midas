package com.cyanspring.apievent.reply;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class ServerReadyEvent extends RemoteAsyncEvent {
	boolean ready;

	public ServerReadyEvent(boolean ready) {
		super(null, null);
		this.ready = ready;
	}

	public boolean isReady() {
		return ready;
	}
	
	
}
