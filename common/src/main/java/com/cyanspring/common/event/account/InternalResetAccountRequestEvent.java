package com.cyanspring.common.event.account;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class InternalResetAccountRequestEvent extends AsyncEvent{
	private ResetAccountRequestEvent event;
	
	public InternalResetAccountRequestEvent(ResetAccountRequestEvent event) {
		this.event = event;
	}

	public ResetAccountRequestEvent getEvent() {
		return event;
	}
}