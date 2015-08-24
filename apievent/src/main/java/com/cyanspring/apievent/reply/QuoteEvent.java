package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Quote;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class QuoteEvent extends RemoteAsyncEvent {
    private Quote quote;
    public QuoteEvent(String key, String receiver, Quote quote) {
        super(key, receiver);
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }
}
