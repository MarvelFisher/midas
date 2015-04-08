package com.cyanspring.common.marketdata;

import com.cyanspring.common.util.PriceUtils;

public class QuoteUtils {
	public static double getMarketablePrice(Quote quote, double qty) {
		return PriceUtils.GreaterThan(qty, 0)?quote.getBid():quote.getAsk();
	}

	public static double getMidPrice(Quote quote) {
		if(PriceUtils.isZero(quote.getBid()))
			return quote.getAsk();
		
		if(PriceUtils.isZero(quote.getAsk()))
			return quote.getBid();
		
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	public static double getLastPrice(Quote quote) {
		if(!PriceUtils.isZero(quote.getLast()))
			return quote.getLast();
		
		if(!PriceUtils.isZero(quote.getClose()))
			return quote.getClose();
		
		return getMidPrice(quote);
	}
	
	public static boolean validQuote(Quote quote) {
		if(PriceUtils.EqualLessThan(quote.getBid(), 0.0) && PriceUtils.EqualLessThan(quote.getAsk(), 0.0))
			return false;
		
		return true;
	}

}
