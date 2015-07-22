package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
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

    private Account account;
    private double cash;
    private AuditType type;
    public PmAddCashEvent(String key, String receiver, Account account, double cash, AuditType type) {
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

    public void setCash(double cash) {
        this.cash = cash;
    }

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
