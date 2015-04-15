package com.cyanspring.apievent.request;

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
public class StrategySnapshotRequestEvent extends RemoteAsyncEvent {
    private String txId;

    public StrategySnapshotRequestEvent(String key, String receiver, String txId) {
        super(key, receiver);
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}
