package com.cyanspring.common.marketdata;

import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.PriceUtils;

public class PriceSessionQuoteChecker extends PriceQuoteChecker{
	
	MarketSessionType session;
	public MarketSessionType getSession() {
		return session;
	}
	
	public void setSession(MarketSessionType session) {
		this.session = session;
	}
	
	public boolean checkWithSession(Quote quote) {
		if (session == MarketSessionType.CLOSE)
			return false;
		
		return check(quote);
	}
}
