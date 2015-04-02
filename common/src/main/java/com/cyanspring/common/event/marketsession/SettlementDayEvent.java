package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.RefData;

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
public class SettlementDayEvent extends RemoteAsyncEvent {

    private RefData refData;
    public SettlementDayEvent(String key, String receiver, RefData refData) {
        super(key, receiver);
        this.refData = refData;
    }

    public RefData getRefData() {
        return refData;
    }
}
