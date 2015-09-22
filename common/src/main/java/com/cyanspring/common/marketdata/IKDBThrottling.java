package com.cyanspring.common.marketdata;

public interface IKDBThrottling {
	boolean isNewQuote(Quote comingQuote, Quote origQuote);
}
