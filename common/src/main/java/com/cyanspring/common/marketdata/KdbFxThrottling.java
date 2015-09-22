package com.cyanspring.common.marketdata;

import com.cyanspring.common.util.PriceUtils;

public class KdbFxThrottling implements IKDBThrottling {

	@Override
	public boolean isNewQuote(Quote comingQuote, Quote origQuote) {
		//accroing price
		if( comingQuote.ask > origQuote.high || 
			(!PriceUtils.isZero(comingQuote.bid) && comingQuote.bid < origQuote.low))
			return true;
		return false;
	}

}
