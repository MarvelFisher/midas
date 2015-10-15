package com.cyanspring.cstw.trader.helper;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/14
 *
 */
public class QuoteHelper {

	public static boolean checkValid(Quote quote) {
		if (PriceUtils.Equal(quote.getAsk(), 0)
				&& (PriceUtils.Equal(quote.getBid(), 0))
				&& (PriceUtils.Equal(quote.getAskVol(), 0))
				&& (PriceUtils.Equal(quote.getBidVol(), 0))
				&& (PriceUtils.Equal(quote.getHigh(), 0))
				&& (PriceUtils.Equal(quote.getLow(), 0))) {
			return false;
		}
		return true;
	}
}
