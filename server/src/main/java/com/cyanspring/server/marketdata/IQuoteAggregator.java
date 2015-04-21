package com.cyanspring.server.marketdata;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionType;

public interface IQuoteAggregator {
			
	public void reset(String symbol);	
	
	public Quote update(String symbol, Quote quote, int sourceId);

	public void onMarketSession(MarketSessionType marketSessionType);
}
