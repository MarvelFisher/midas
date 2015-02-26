package com.cyanspring.server.marketdata;

import java.util.HashMap;

import com.cyanspring.common.marketdata.Quote;

public class QuoteAggregator {
	
	
	HashMap<String, AggrQuote> table = new HashMap<String, AggrQuote>(); 
	
	AggrQuote getQuote(String symbol) {
		if (table.containsKey(symbol)) {
			return table.get(symbol);		
		}
		else {
			AggrQuote aggrQuote = new AggrQuote(symbol);
			table.put(symbol,aggrQuote);
			return aggrQuote;
		}
	}	
	
	public void reset(String symbol) {
		AggrQuote aggrQuote = getQuote(symbol);
		if (null != aggrQuote) {
			aggrQuote.reset();
		}
	}
	
	public Quote update(String symbol, Quote quote, int sourceId) {
		AggrQuote aggrQuote = getQuote(symbol);
		return aggrQuote.update(quote, sourceId);
		
	}
}
