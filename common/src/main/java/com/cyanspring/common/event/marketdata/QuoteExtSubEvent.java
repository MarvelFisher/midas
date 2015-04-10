package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QuoteExtSubEvent  extends RemoteAsyncEvent{

	public QuoteExtSubEvent(String key, String receiver) {
		super(key, receiver);
	}

}
