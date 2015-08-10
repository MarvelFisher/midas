package com.cyanspring.common.marketdata;

/**
 * Created by FDT on 15/4/8.
 */
public class InnerQuote {
    private Quote quote;
    private QuoteSource quoteSource;
    private String contributor;
    private long throwQuoteTimeInterval = 0;

    public InnerQuote(QuoteSource quoteSource, Quote quote){
        this.quote = quote;
        this.quoteSource = quoteSource;
    }

    public Quote getQuote() {
        return quote;
    }

    public QuoteSource getQuoteSource() {
        return quoteSource;
    }

    public String getSymbol(){
        return quote.getSymbol();
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public long getThrowQuoteTimeInterval() {
        return throwQuoteTimeInterval;
    }

    public void setThrowQuoteTimeInterval(long throwQuoteTimeInterval) {
        this.throwQuoteTimeInterval = throwQuoteTimeInterval;
    }
}
