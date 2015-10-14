package com.cyanspring.common.marketdata;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Calvin on 15/4/21.<br/>
 * Modify By Shuwei on 15/9/8.
 */
public class QuoteAggregatorSimple implements IQuoteAggregator {

    ConcurrentHashMap<String, QuoteS> quotes = new ConcurrentHashMap<String, QuoteS>();

    QuoteS getQuote(String symbol, Quote quote, QuoteSource quoteSource) {
        QuoteS q = quotes.get(symbol);
        if (q == null) {
            q = new QuoteS(quote, quoteSource);
            quotes.put(symbol, q);
        }
        return q;
    }

    public void reset(String symbol) {
        QuoteS q = quotes.get(symbol);
        if (q != null) {
            q.reset();
        }
    }

    public Quote update(String symbol, Quote quote, QuoteSource quoteSource) {
        QuoteS q = getQuote(symbol, quote, quoteSource);
        q.update(quote, quoteSource);
        return q.quote;
    }
}


class QuoteS {
    public Quote quote;
    QuoteSource quoteSource = QuoteSource.DEFAULT;

    QuoteS(Quote quote, QuoteSource quoteSource) {
        this.quote = quote;
        this.quoteSource = quoteSource;
    }

    public Quote update(Quote quote, QuoteSource quoteSource) {

        if (quote.getAsk() <= 0 || quote.getBid() <= 0) {
            return this.quote;
        }
        if (quoteSource != QuoteSource.IB) {  // not major quote , use only bid / ask. unless there is no pre-close in this.quote
            this.quote.setBid(quote.getBid());
            this.quote.setAsk(quote.getAsk());
            if (quote.getAsk() > this.quote.getHigh()) {
                this.quote.setHigh(quote.getAsk());
            }
            if (quote.getBid() < this.quote.getLow() || this.quote.getLow() == 0) {
                this.quote.setLow(quote.getBid());
            }
            this.quote.setLast((quote.getBid() + quote.getAsk())/2.0);
        } else {
            if (quote.getAsk() > 0 && quote.getBid() > 0) {
                this.quote = quote;
            }
        }
        this.quote.setTimeStamp(new Date());
        this.quoteSource = quoteSource;
        return this.quote;
    }

    public void reset() {
        quoteSource = QuoteSource.DEFAULT;  // accept quote from all source
    }
}
