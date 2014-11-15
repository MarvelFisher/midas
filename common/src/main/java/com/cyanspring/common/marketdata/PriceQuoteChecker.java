package com.cyanspring.common.marketdata;

import com.cyanspring.common.util.PriceUtils;

public class PriceQuoteChecker implements IQuoteChecker {

	@Override
	public boolean check(Quote quote) {
		if(PriceUtils.GreaterThan(quote.getBid(), 0) && PriceUtils.GreaterThan(quote.getAsk(), 0))
			return true;
		return false;
	}
}
