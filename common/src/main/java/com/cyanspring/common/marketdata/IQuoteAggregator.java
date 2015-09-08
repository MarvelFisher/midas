package com.cyanspring.common.marketdata;

import com.cyanspring.common.marketsession.MarketSessionType;

public interface IQuoteAggregator {
			
	void reset(String symbol);
	
	Quote update(String symbol, Quote quote, QuoteSource quoteSource);

}
