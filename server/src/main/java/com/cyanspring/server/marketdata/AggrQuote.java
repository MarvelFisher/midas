package com.cyanspring.server.marketdata;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.TimeThrottler;

public class AggrQuote {

	TimeThrottler timer;
	double minAsk = 0, maxBid = 0, maxBidAsk = 0;
	double open = 0, high = 0, low = 0, close = 0;
	Quote quote = null;
	int sourceId = -1;

	public AggrQuote(String symbol, long inteval) {
		timer = new TimeThrottler(inteval);
		minAsk = maxBid = maxBidAsk = 0;
	}

	void clearQuote() {
		minAsk = maxBid = maxBidAsk = 0;
	}
	public Quote updateQuote(Quote src) {

		if (quote == null) {
			quote = src;
		}

		if (maxBid < src.getBid()) {
			maxBid = src.getBid();
			maxBidAsk = src.getAsk();
		}
		double ask = Math.min(src.getAsk(), maxBidAsk);
		minAsk = Math.max(ask, minAsk);
		
		if (src.sourceId == sourceId && src.getClose() != close) {
			open = high = low = 0;		
			sourceId = src.sourceId;			
		}
		
		if (close == 0 || src.sourceId == 1) {		
			close = src.getClose();	
		}		
		
		if (open == 0 || (src.sourceId == 1 && src.getOpen() != 0)) {
			open = src.getOpen();					
		}
		
		if (low == 0 || low > src.getLow()) {
			low = src.getLow();
		}
		
		high = Math.max(high, src.getHigh());		
		
		if (timer.check()) {
			Quote retQuote = src;
			
			double last = (maxBid + minAsk) / 2;
			retQuote.setBid(maxBid);
			retQuote.setAsk(minAsk);
			retQuote.setLast(last);
			
			retQuote.setOpen(open);
			retQuote.setHigh(high);
			retQuote.setLow(low);
			retQuote.setClose(close);
			
			clearQuote();
			
			return retQuote;
			
		} else {
			return null;
		}
	}

}
