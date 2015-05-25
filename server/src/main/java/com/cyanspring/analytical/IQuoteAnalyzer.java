package com.cyanspring.analytical;

import java.util.Map;

import com.cyanspring.common.marketdata.Quote;

public interface IQuoteAnalyzer {
	void init();
	void uninit();
	void analyze(Quote quote);
}
