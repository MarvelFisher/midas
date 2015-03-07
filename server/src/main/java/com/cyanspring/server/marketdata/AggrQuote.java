package com.cyanspring.server.marketdata;

import com.cyanspring.common.marketdata.Quote;

public class AggrQuote {

	// TimeThrottler timer;
	double minAsk = 0, maxBid = 0, maxBidAsk = 0;
	double open = 0, high = 0, low = 0, close = 0;
	Quote quote = null;
	int sourceId = -1;

	public AggrQuote(String symbol) {

		minAsk = maxBid = maxBidAsk = 0;
	}

	public void reset() {
		minAsk = maxBid = maxBidAsk = 0;
	}

	public Quote update(Quote src, int sourceId) {

		if (quote == null) {
			quote = src;
		}

		if (maxBid < src.getBid()) {
			maxBid = src.getBid();
			maxBidAsk = src.getAsk();
		}

		minAsk = minAsk != 0 ? Math.min(src.getAsk(), minAsk) : src.getAsk();

		if (src.sourceId == sourceId && src.getClose() != close) {
			open = high = low = 0;
			this.sourceId = sourceId;
		}

		if (src.sourceId == 1) {

			if (src.getClose() != 0) {
				close = src.getClose();
			}

			if (src.getOpen() != 0) {
				open = src.getOpen();
			}

			if (src.getLow() != 0) {
				low = src.getLow();
			}

			if (src.getHigh() != 0) {
				high = src.getHigh();
			}

			this.sourceId = sourceId;
		} else {

			if (close == 0 || sourceId == src.sourceId) {
				close = src.getClose();
				this.sourceId = sourceId;
			}

			if (open == 0 || sourceId == src.sourceId) {
				open = src.getOpen();
				this.sourceId = sourceId;
			}

			if (low == 0 || sourceId == src.sourceId) {
				low = src.getLow();
				this.sourceId = sourceId;
			}

			if (high == 0 || sourceId == src.sourceId) {
				high = src.getHigh();
				this.sourceId = sourceId;
			}
		}

		Quote retQuote = src;
		if (minAsk <= maxBid) {
			minAsk = maxBidAsk;
		}

		double last = (maxBid + minAsk) / 2;
		retQuote.setBid(maxBid);
		retQuote.setAsk(minAsk);
		retQuote.setLast(last);

		retQuote.setOpen(open);
		retQuote.setHigh(high);
		retQuote.setLow(low);
		retQuote.setClose(close);
		
		return retQuote;

	}

}
