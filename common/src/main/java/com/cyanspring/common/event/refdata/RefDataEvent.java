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
    private boolean ok;
    public RefDataEvent(String key, String receiver, List<RefData> refDataList, boolean ok) {
        super(key, receiver);
        this.refDataList = refDataList;
        this.ok = ok;
    }

    public List<RefData> getRefDataList() {
        return refDataList;
    }

    public boolean isOk() {
        return ok;
    }
}
