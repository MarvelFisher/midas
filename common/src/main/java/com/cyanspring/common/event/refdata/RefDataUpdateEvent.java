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
 * @author Shuwei
 * @version %I%, %G%
 * @since 1.0
 */
public class RefDataUpdateEvent extends RemoteAsyncEvent {
    public enum Action{
        ADD,DEL
    }
    private List<RefData> refDataList;
    private Action action;
    public RefDataUpdateEvent(String key, String receiver, List<RefData> refDataList, Action action) {
        super(key, receiver);
        this.refDataList = refDataList;
        this.action = action;
    }
    public List<RefData> getRefDataList() {
        return refDataList;
    }
}
