package com.cyanspring.cstw.event;

import com.cyanspring.common.event.AsyncEvent;

public class QuoteSymbolSelectEvent extends AsyncEvent{
	
	private static final long serialVersionUID = 1L;
	private String symbol;
	
	public QuoteSymbolSelectEvent(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
