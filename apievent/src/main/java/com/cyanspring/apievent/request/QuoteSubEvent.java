package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class QuoteSubEvent extends RemoteAsyncEvent {
    String symbol;

    public QuoteSubEvent(String key, String receiver, String symbol) {
        super(key, receiver);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
