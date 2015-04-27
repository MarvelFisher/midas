package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Date;

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
public class IndexSessionRequestEvent extends RemoteAsyncEvent {
    private String index;
    private Date date;
    public IndexSessionRequestEvent(String key, String receiver, String index, Date date) {
        super(key, receiver);
        this.index = index;
        this.date = date;
    }

    public String getIndex() {
        return index;
    }

    public Date getDate() {
        return date;
    }
}
