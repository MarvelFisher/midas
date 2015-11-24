package com.cyanspring.cstw.localevent;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.marketdata.Quote;

public class MarketDataReplyLocalEvent extends AsyncEvent{
	private static final long serialVersionUID = 1L;
	private Quote quote;
	
	public MarketDataReplyLocalEvent(Quote quote) {
		this.quote = quote;
	}

	public Quote getQuote() {
		return quote;
	}
}
