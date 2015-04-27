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
public class AllMarketSessionReplyEvent extends RemoteAsyncEvent{
    private Map<String, MarketSessionData> dataMap;
    public AllMarketSessionReplyEvent(String key, String receiver, Map<String, MarketSessionData> dataMap) {
        super(key, receiver);
        this.dataMap = dataMap;
    }

    public Map<String, MarketSessionData> getDataMap() {
        return dataMap;
    }
}
