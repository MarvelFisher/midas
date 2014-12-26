package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class TradeDateUpdateEvent extends RemoteAsyncEvent {
	private String tradeDate;

	public TradeDateUpdateEvent(String key, String receiver, String tradeDate) {
		super(key, receiver);
		this.tradeDate = tradeDate;
	}

	public String getTradeDate() {
		return tradeDate;
	}

}
