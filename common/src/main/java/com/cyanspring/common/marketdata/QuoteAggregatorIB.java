package com.cyanspring.common.marketdata;

import com.cyanspring.common.marketsession.MarketSessionType;

import java.util.concurrent.ConcurrentHashMap;

public class QuoteAggregatorIB implements IQuoteAggregator {
	
	ConcurrentHashMap<String, QuoteMS> table = new ConcurrentHashMap<String, QuoteMS>();
	
	QuoteMS getQuote(String symbol,Quote quote,int sourceId) {
		QuoteMS q = table.get(symbol);
		if(q == null) {
			q = new QuoteMS(quote,sourceId);
			table.put(symbol, q);
		}
		return q;
	}
	
	public void reset(String symbol) {
		QuoteMS q = table.get(symbol);
		if(q != null) {
			q.reset();
		}
	}
	
	public Quote update(String symbol, Quote quote, int sourceId) {
		QuoteMS q = getQuote(symbol, quote, sourceId);
		q.update(quote, sourceId);
		return q.quote; 
	}

	public void onMarketSession(MarketSessionType marketSessionType){

	}
}

class QuoteMS {
	public Quote quote;
	int sourceId = -1;
	double last; 
	double gap = 0;
	
	QuoteMS(Quote quote,int sourceId) {
		this.quote = quote;		
		this.sourceId = sourceId;
		last = (this.quote.getAsk() + this.quote.getBid()) / 2;
	}
	
	public Quote update(Quote quote, int sourceId) {
		
		if(sourceId == 1 || this.sourceId == -1) {  // Major Quote or first quote
			if(sourceId != 1) {  // not major quote , use only bid / ask. unless there is no pre-close in this.quote
				if(quote.getClose() == 0) {  // getClose : get pre close at forex
					this.quote = quote;
				} else {
					this.quote.setBid(quote.getBid());
					this.quote.setAsk(quote.getAsk());
				}					
			} else { 				
				this.quote = quote;
			}			
			this.sourceId = sourceId;
		} else  if(this.sourceId != 1 ) {		
			if(quote.getAsk() == 0 || quote.getBid() == 0) {
				return quote;
			}
			double diff = Math.abs((quote.getAsk() + quote.getBid()) / 2 - last);
			if(diff < gap || gap == 0) {
				this.quote.setAsk(quote.getAsk());
				this.quote.setBid(quote.getBid());
				gap = diff;
			} 
		}					
		return this.quote;
	}
	
	public void reset() {
		last = (quote.getBid() + quote.getAsk()) / 2;
		gap = 0;
		sourceId = -1;  // accept quote from all source  
	}

}


