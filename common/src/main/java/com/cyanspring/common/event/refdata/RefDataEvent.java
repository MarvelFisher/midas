package com.cyanspring.common.event.refdata;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.RefData;

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
public class RefDataEvent extends RemoteAsyncEvent {
    private List<RefData> refDataList;
    public RefDataEvent(String key, String receiver, List<RefData> refDataList) {
        super(key, receiver);
        this.refDataList = refDataList;
    }

    public List<RefData> getRefDataList() {
        return refDataList;
    }
}
