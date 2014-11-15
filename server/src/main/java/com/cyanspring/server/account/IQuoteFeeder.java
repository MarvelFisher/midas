package com.cyanspring.server.account;

import com.cyanspring.common.marketdata.Quote;

public interface IQuoteFeeder {
	Quote getQuote(String symbol);
}
