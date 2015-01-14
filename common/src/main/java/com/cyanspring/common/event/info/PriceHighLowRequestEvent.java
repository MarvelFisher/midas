package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PriceHighLowRequestEvent extends RemoteAsyncEvent {
	private PriceHighLowType type;
	private String symbol ;

	public PriceHighLowRequestEvent(String key, String receiver,
			PriceHighLowType type,
			String symbol) {
		super(key, receiver);
		this.type = type;
		this.symbol = symbol ;
	}

	public PriceHighLowType getType() {
		return type;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}