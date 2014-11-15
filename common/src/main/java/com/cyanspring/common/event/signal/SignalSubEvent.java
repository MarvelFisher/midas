package com.cyanspring.common.event.signal;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SignalSubEvent extends RemoteAsyncEvent {
	private String symbol;
	
	public SignalSubEvent(String key, String receiver, String symbol) {
		super(key, receiver);
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
