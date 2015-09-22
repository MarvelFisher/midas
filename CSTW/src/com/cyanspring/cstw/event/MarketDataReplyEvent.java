package com.cyanspring.cstw.event;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.marketdata.Quote;

public class MarketDataReplyEvent extends AsyncEvent{
	private static final long serialVersionUID = 1L;
	private Quote quote;
	
	public MarketDataReplyEvent(Quote quote) {
		this.quote = quote;
	}

	public Quote getQuote() {
		return quote;
	}
}
