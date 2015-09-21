package com.cyanspring.common.marketdata;

import com.cyanspring.common.util.PriceUtils;

public class KdbDefaultThrottling implements IKDBThrottling {

	@Override
	public boolean isNewQuote(Quote comingQuote, Quote origQuote) {
		//tick with volume changed
		if( comingQuote.totalVolume > origQuote.totalVolume)
			return true;
		return false;
	}

}
