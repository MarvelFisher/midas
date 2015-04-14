package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.ClientEvent;
import com.cyanspring.apievent.obj.Quote;

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
public class QuoteEvent extends ClientEvent {
    private Quote quote;
    public QuoteEvent(String key, String receiver, Quote quote) {
        super(key, receiver);
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }
}
