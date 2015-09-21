package com.cyanspring.common.event.statistic;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class TickTableRequestEvent extends RemoteAsyncEvent {

	public TickTableRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
