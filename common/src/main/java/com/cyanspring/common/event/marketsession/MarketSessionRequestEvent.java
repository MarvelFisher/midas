package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class MarketSessionRequestEvent  extends RemoteAsyncEvent {

	public MarketSessionRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
