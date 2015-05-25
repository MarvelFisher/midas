package com.cyanspring.adaptor;

import com.cyanspring.common.marketdata.IQuoteAggregator;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Calvin on 15/4/21.
 */
public class QuoteAggregatorSimple implements IQuoteAggregator {
    private MarketSessionType marketSessionType = MarketSessionType.DEFAULT;

    ConcurrentHashMap<String, QuoteS> table = new ConcurrentHashMap<String, QuoteS>();

    QuoteS getQuote(String symbol, Quote quote, int sourceId) {
        QuoteS q = table.get(symbol);
        if (q == null) {
            q = new QuoteS(quote, sourceId);
            table.put(symbol, q);
        }
        return q;
    }

    public void reset(String symbol) {
        QuoteS q = table.get(symbol);
        if (q != null) {
            q.reset();
        }
    }

    public Quote update(String symbol, Quote quote, int sourceId) {
        QuoteS q = getQuote(symbol, quote, sourceId);
        q.update(quote, sourceId);
        return q.quote;
    }


    public void onMarketSession(MarketSessionType marketSessionType) {
        if (marketSessionType == this.marketSessionType) {
            return;
        }
        this.marketSessionType = marketSessionType;
        if (this.marketSessionType == MarketSessionType.PREOPEN) {
            for (String symbol : table.keySet()) {
                table.get(symbol).sunrise();
            }
        }

    }
}


class QuoteS {
    public Quote quote;
    int sourceId = -1;
    double last;
    double gap = 0;

    QuoteS(Quote quote, int sourceId) {
        this.quote = quote;
        this.sourceId = sourceId;
        last = (this.quote.getAsk() + this.quote.getBid()) / 2;
    }

    public void sunrise() {
        quote.setClose(quote.getLast());
        quote.setOpen(0);
        quote.setHigh(0);
        quote.setLow(0);
        quote.setLast(0);
        quote.setTotalVolume(0);
    }

    public Quote update(Quote quote, int sourceId) {

        if (quote.getAsk() <= 0 || quote.getBid() <= 0) {
            return this.quote;
        }
        last = (quote.getAsk() + quote.getBid()) / 2;
        quote.setLast(last);
        if (sourceId != 1) {  // not major quote , use only bid / ask. unless there is no pre-close in this.quote
            this.quote.setBid(quote.getBid());
            this.quote.setAsk(quote.getAsk());
            if (this.quote.getOpen() == 0) {
                this.quote.setOpen(last);
            }
            if (quote.getAsk() > this.quote.getHigh()) {
                this.quote.setHigh(quote.getAsk());
            }
            if (quote.getBid() < this.quote.getLow() || this.quote.getLow() == 0) {
                this.quote.setLow(quote.getBid());
            }
        } else {
            if (quote.getAsk() > 0 && quote.getBid() > 0) {
                this.quote = quote;
            }
        }
        this.quote.setTimeStamp(quote.getTimeStamp());
        this.sourceId = sourceId;
        return this.quote;
    }

    public void reset() {
        last = (quote.getBid() + quote.getAsk()) / 2;
        gap = 0;
        sourceId = -1;  // accept quote from all source
    }

}
