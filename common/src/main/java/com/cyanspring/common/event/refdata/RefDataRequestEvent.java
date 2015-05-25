package com.cyanspring.common.event.refdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

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
public class RefDataRequestEvent extends RemoteAsyncEvent{
    public RefDataRequestEvent(String key, String receiver) {
        super(key, receiver);
    }
}
