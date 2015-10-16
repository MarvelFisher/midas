package com.cyanspring.common.marketdata;

import com.cyanspring.common.data.DataObject;

public interface IQuoteListener {
	void init() throws Exception;
	void uninit() throws Exception;
	void onQuote(InnerQuote InnerQuote);
	void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource);
	void onTrade(Trade trade);
    String getId();
}
