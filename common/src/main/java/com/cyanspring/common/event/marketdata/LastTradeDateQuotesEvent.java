package com.cyanspring.common.event.marketdata;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.Quote;

public class LastTradeDateQuotesEvent extends RemoteAsyncEvent {
	private String tradeDate;
	private String indexSessionName;
	private List<Quote> quotes;

	public LastTradeDateQuotesEvent(String key, String receiver, String indexSessionName
			,String tradeDate, List<Quote> quotes) {
		super(key, receiver);
		this.indexSessionName = indexSessionName;
		this.tradeDate = tradeDate;
		this.quotes = quotes;
	}
	public String getIndexSessionName() {
		return indexSessionName;
	}
	public String getTradeDate() {
		return tradeDate;
	}
	public List<Quote> getQuotes() {
		return quotes;
	}
	
	
}
