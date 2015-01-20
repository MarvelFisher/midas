package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SymbolRequestEvent extends RemoteAsyncEvent {

	public SymbolRequestEvent(String key, String receiver) {
		super(key, receiver);
	}
}
