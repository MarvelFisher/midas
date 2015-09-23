package com.cyanspring.common.event.statistic;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class TickTableRequestEvent extends RemoteAsyncEvent {

	private String symbol;
	public TickTableRequestEvent(String key, String receiver,String symbol) {
		super(key, receiver);
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
}
