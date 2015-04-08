package com.cyanspring.common.marketdata;

/**
 * Created by FDT on 15/4/8.
 */
public class InnerQuote {
    private Quote quote;
    private int sourceId;
    private String contributor;

    public InnerQuote(int sourceId, Quote quote){
        this.quote = quote;
        this.sourceId = sourceId;
    }

    public Quote getQuote() {
        return quote;
    }

    public int getSourceId() {
        return sourceId;
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
}
