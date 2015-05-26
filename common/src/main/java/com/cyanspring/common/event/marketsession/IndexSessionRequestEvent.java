package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Date;
import java.util.List;

/**
 * This event is used to request detail marketSession data
 * Subscriber: IndexMarketSessionManager.java
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class IndexSessionRequestEvent extends RemoteAsyncEvent {
    private List<String> indexList;
    private Date date;
    public IndexSessionRequestEvent(String key, String receiver, List<String> indexList, Date date) {
        super(key, receiver);
        this.indexList = indexList;
        this.date = date;
    }

    public List<String> getIndexList() {
        return indexList;
    }

    public Date getDate() {
        return date;
    }
}
