package com.cyanspring.common.marketdata;

import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;

public class QuoteUtils {
	public static double getMarketablePrice(Quote quote, double qty) {
		return PriceUtils.GreaterThan(qty, 0)?quote.getBid():quote.getAsk();
	}

	public static double getMarketablePrice(Quote quote, OrderSide side) {
		return side.isBuy()?quote.getAsk():quote.getBid();
	}

	public static double getMidPrice(Quote quote) {
		if(!PriceUtils.GreaterThan(quote.getBid(), 0.0))
			return quote.getAsk();
		
		if(!PriceUtils.GreaterThan(quote.getAsk(), 0.0))
			return quote.getBid();
		
		return (quote.getBid() + quote.getAsk())/2;
	}
	
	public static double getLastPrice(Quote quote) {
		if(PriceUtils.GreaterThan(quote.getLast(), 0.0))
			return quote.getLast();
		
		if(PriceUtils.GreaterThan(quote.getClose(), 0.0))
			return quote.getClose();
		
		return getMidPrice(quote);
	}
	
	public static boolean validQuote(Quote quote) {
		if(PriceUtils.EqualLessThan(quote.getBid(), 0.0) && PriceUtils.EqualLessThan(quote.getAsk(), 0.0))
			return false;
		
		return true;
	}

	public static double getValidPrice(Quote quote) {
		double price = getMidPrice(quote);
		
		if(!PriceUtils.GreaterThan(price, 0.0)) {
			price = quote.getLast();
		}
		
		if(!PriceUtils.GreaterThan(price, 0.0)) {
			price = quote.getClose();
		}
		
		return price;
	}
	
	public static double getPnlPrice(Quote quote, double qty, boolean useMid) {
		if(quote.isStale()) {
			double price = 0.0;
			if(useMid) {
				price = getMidPrice(quote);
				if(!PriceUtils.validPrice(price))
					price = quote.getLast();
			} else {
				price = quote.getLast();
				if(!PriceUtils.validPrice(price))
					price = getMidPrice(quote);
			}
			return price;
		} else {
			return getMarketablePrice(quote, qty);
		}
	}
}
