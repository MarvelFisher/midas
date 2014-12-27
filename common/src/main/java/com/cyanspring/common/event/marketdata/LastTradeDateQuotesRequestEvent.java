package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class LastTradeDateQuotesRequestEvent extends RemoteAsyncEvent {

	public LastTradeDateQuotesRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
