package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class TradeDateRequestEvent  extends RemoteAsyncEvent {

	public TradeDateRequestEvent(String key, String receiver) {
		super(key, receiver);
	}

}
