package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.MarketSessionData;

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
public class IndexSessionEvent extends RemoteAsyncEvent{
    private Map<String, MarketSessionData> dataMap;
    private boolean ok;
    public IndexSessionEvent(String key, String receiver, Map<String, MarketSessionData> dataMap, boolean ok) {
        super(key, receiver);
        this.dataMap = dataMap;
        this.ok = ok;
    }

    public Map<String, MarketSessionData> getDataMap() {
        return dataMap;
    }

    public boolean isOk() {
        return ok;
    }
}
