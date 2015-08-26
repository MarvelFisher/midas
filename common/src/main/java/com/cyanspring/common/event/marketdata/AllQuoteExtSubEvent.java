package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AllQuoteExtSubEvent extends RemoteAsyncEvent{

	public AllQuoteExtSubEvent(String key, String receiver) {
		super(key, receiver);
	}

}
