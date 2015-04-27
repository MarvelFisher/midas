package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Date;
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
public class AllMarketSessionRequestEvent extends RemoteAsyncEvent {
    private List<String> indexList;
    private Date date;
    public AllMarketSessionRequestEvent(String key, String receiver, List<String> indexList, Date date) {
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
