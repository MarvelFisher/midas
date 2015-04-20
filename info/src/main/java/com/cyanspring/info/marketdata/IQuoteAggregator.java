package com.cyanspring.info.marketdata;

import com.cyanspring.common.marketdata.Quote;

public interface IQuoteAggregator {
			
	public void reset(String symbol);	
	
	public Quote update(String symbol, Quote quote, int sourceId);

}
