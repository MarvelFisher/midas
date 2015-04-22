package com.cyanspring.common.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;

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
public class QuoteRequestEvent extends RemoteAsyncEvent{
    private List<String> symbolList;
    public QuoteRequestEvent(String key, String receiver, List<String> symbolList) {
        super(key, receiver);
        this.symbolList = symbolList;
    }

    public List<String> getSymbolList() {
        return symbolList;
    }
}
