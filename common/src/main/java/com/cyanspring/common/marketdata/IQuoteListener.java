package com.cyanspring.common.marketdata;

import com.cyanspring.common.data.DataObject;

public interface IQuoteListener {
	public void init() throws Exception;
	public void uninit() throws Exception;
	public void onQuote(InnerQuote InnerQuote);
	void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource);
	void onTrade(Trade trade);
}
