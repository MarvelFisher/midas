package com.cyanspring.apievent.request;

import com.cyanspring.apievent.ClientEvent;

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
public class QuoteSubEvent extends ClientEvent{
    String symbol;

    public QuoteSubEvent(String key, String receiver, String symbol) {
        super(key, receiver);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
