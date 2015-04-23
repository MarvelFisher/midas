package com.cyanspring.common.marketdata;

import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.util.PriceUtils;

public class QuoteChecker implements IQuoteChecker {

    MarketSessionType session;
    private boolean quotePriceWarningIsOpen = false;
    private int quotePriceWarningPercent = 99;

    public MarketSessionType getSession() {
        return session;
    }

    public void setQuotePriceWarningIsOpen(boolean quotePriceWarningIsOpen) {
        this.quotePriceWarningIsOpen = quotePriceWarningIsOpen;
    }

    public void setQuotePriceWarningPercent(int quotePriceWarningPercent) {
        this.quotePriceWarningPercent = quotePriceWarningPercent;
    }

    public void setSession(MarketSessionType session) {
        this.session = session;
    }

    // Check Quote Value
    public boolean checkAndUpdateQuote(Quote prev, Quote quote) {
        boolean IsCorrectQuote = true;
        if (prev != null) {
            if (PriceUtils.EqualLessThan(quote.getClose(), 0)) {
                quote.setClose(prev.getClose());
            }
            if (PriceUtils.EqualLessThan(quote.getOpen(), 0)) {
                quote.setOpen(prev.getOpen());
            }
            if (PriceUtils.EqualLessThan(quote.getHigh(), 0)) {
                quote.setHigh(prev.getHigh());
            }
            if (PriceUtils.EqualLessThan(quote.getLow(), 0)) {
                quote.setLow(prev.getLow());
            }
            if (PriceUtils.EqualLessThan(quote.getBid(), 0)) {
                quote.setBid(prev.getBid());
            }
            if (PriceUtils.EqualLessThan(quote.getAsk(), 0)) {
                quote.setAsk(prev.getAsk());
            }
        }

        if (this.quotePriceWarningIsOpen) {
            if (PriceUtils.GreaterThan(quote.getClose(), 0)
                    && this.quotePriceWarningPercent > 0
                    && this.quotePriceWarningPercent < 100) {
                double preCloseAddWarningPrice = quote.getClose()
                        * (1.0 + this.quotePriceWarningPercent / 100.0);
                double preCloseSubtractWarningPrice = quote.getClose()
                        * (1.0 - this.quotePriceWarningPercent / 100.0);
                if (PriceUtils.GreaterThan(quote.getAsk(), 0)
                        && (PriceUtils.GreaterThan(quote.getAsk(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getAsk(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (PriceUtils.GreaterThan(quote.getBid(), 0)
                        && (PriceUtils.GreaterThan(quote.getBid(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getBid(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (PriceUtils.GreaterThan(quote.getHigh(), 0)
                        && (PriceUtils.GreaterThan(quote.getHigh(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getHigh(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (PriceUtils.GreaterThan(quote.getLow(), 0)
                        && (PriceUtils.GreaterThan(quote.getLow(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getLow(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
                if (PriceUtils.GreaterThan(quote.getOpen(), 0)
                        && (PriceUtils.GreaterThan(quote.getOpen(),
                        preCloseAddWarningPrice) || PriceUtils
                        .LessThan(quote.getOpen(),
                                preCloseSubtractWarningPrice))) {
                    IsCorrectQuote = false;
                }
            }
        }

        return IsCorrectQuote;
    }

    @Override
    public boolean check(Quote quote) {
        if (session == MarketSessionType.CLOSE)
            return false;
        return true;
    }
}
