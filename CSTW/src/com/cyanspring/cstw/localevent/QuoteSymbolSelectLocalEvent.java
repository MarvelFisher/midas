package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;

public class QuoteSymbolSelectLocalEvent extends AsyncEvent{
	
	private static final long serialVersionUID = 1L;
	private String symbol;
	
	public QuoteSymbolSelectLocalEvent(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
