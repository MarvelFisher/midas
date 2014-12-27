package com.cyanspring.common.event.marketdata;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.Quote;

public class LastTradeDateQuotesEvent extends RemoteAsyncEvent {
	private String tradeDate;
	private List<Quote> quotes;

	public LastTradeDateQuotesEvent(String key, String receiver,
			String tradeDate, List<Quote> quotes) {
		super(key, receiver);
		this.tradeDate = tradeDate;
		this.quotes = quotes;
	}
	public String getTradeDate() {
		return tradeDate;
	}
	public List<Quote> getQuotes() {
		return quotes;
	}
	
	
}
