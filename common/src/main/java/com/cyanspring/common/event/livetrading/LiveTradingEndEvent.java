package com.cyanspring.common.event.livetrading;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class LiveTradingEndEvent extends RemoteAsyncEvent{

	public LiveTradingEndEvent(String key, String receiver) {
		
		super(key, receiver);

	}

}
