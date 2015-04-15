package com.cyanspring.event.api.obj;

import com.cyanspring.common.transport.IUserSocketContext;

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
public class PendingRecord {
    public String txId;
    public String origTxId;
    public IUserSocketContext ctx;

    public PendingRecord(String txId, String origTxId, IUserSocketContext ctx) {
        this.txId = txId;
        this.origTxId = origTxId;
        this.ctx = ctx;
    }
}
