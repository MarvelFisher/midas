package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.MarketSessionData;

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
public class IndexSessionEvent extends RemoteAsyncEvent{
    private String index;
    private MarketSessionData data;
    public IndexSessionEvent(String key, String receiver, String index, MarketSessionData data) {
        super(key, receiver);
        this.index = index;
        this.data = data;
    }

    public MarketSessionData getData() {
        return data;
    }

    public String getIndex() {
        return index;
    }
}
