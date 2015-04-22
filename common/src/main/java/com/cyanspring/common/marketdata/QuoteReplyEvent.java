package com.cyanspring.common.marketdata;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class QuoteReplyEvent extends RemoteAsyncEvent{
    private Map<String, Quote> quotes;
    private Map<String, DataObject> quoteExts;
    public QuoteReplyEvent(String key, String receiver, Map<String, Quote> quotes, Map<String, DataObject> quoteExts) {
        super(key, receiver);
        this.quotes = quotes;
        this.quoteExts = quoteExts;
    }

    public Map<String, Quote> getQuotes() {
        return quotes;
    }

    public Map<String, DataObject> getQuoteExts() {
        return quoteExts;
    }
}
