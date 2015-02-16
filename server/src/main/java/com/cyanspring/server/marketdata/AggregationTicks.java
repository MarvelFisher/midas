package com.cyanspring.server.marketdata;

import java.util.HashMap;

import com.cyanspring.common.marketdata.Quote;

public class AggregationTicks {
	
	long interval;

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
	HashMap<String, AggrQuote> table = new HashMap<String, AggrQuote>(); 
	
	AggrQuote getQuote(String symbol) {
		if (table.containsKey(symbol)) {
			return table.get(symbol);		
		}
		else {
			AggrQuote aggrQuote = new AggrQuote(symbol, interval);
			table.put(symbol,aggrQuote);
			return aggrQuote;
		}
	}
	
	
	public Quote updateQuote(String symbol, Quote quote) {
		AggrQuote aggrQuote = getQuote(symbol);
		return aggrQuote.updateQuote(quote);
		
	}
	
	
	

}
