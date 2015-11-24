package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

public class MarketDataRequestLocalEvent extends AsyncEvent{	
	private static final long serialVersionUID = 1L;
	private String symbol;
	
	public MarketDataRequestLocalEvent(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
