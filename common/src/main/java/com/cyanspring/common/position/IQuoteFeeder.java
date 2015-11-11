package com.cyanspring.common.position;

import com.cyanspring.common.marketdata.Quote;

public interface IQuoteFeeder {
	Quote getQuote(String symbol);
}
