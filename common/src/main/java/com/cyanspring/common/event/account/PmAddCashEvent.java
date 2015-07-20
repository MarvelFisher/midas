package com.cyanspring.common.event.account;

import com.cyanspring.common.business.AuditType;
import com.cyanspring.common.business.CashAudit;
import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 *
 * Add cash event for internal use
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class PmAddCashEvent extends RemoteAsyncEvent{

    private String account;
    private double cash;
    private AuditType type;
    public PmAddCashEvent(String key, String receiver, String account, double cash, AuditType type) {
        super(key, receiver);
        this.account = account;
        this.cash = cash;
        this.type = type;
    }

    public AuditType getType() {
        return type;
    }

    public void setType(AuditType type) {
        this.type = type;
    }

    public double getCash() {
        return cash;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }
}
