package com.cyanspring.cstw.event;

import com.cyanspring.common.event.AsyncEvent;

public class MarketDataRequestEvent extends AsyncEvent{	
	private static final long serialVersionUID = 1L;
	private String symbol;
	
	public MarketDataRequestEvent(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
