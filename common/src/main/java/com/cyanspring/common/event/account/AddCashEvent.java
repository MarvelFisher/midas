package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 *
 * Add cash event for external use
 *
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class AddCashEvent extends RemoteAsyncEvent{
    private String account;
    private double cash;

    public AddCashEvent(String key, String receiver, String account, double cash) {
        super(key, receiver);
        this.account = account;
        this.cash = cash;
    }

    public String getAccount() {
        return account;
    }

    public double getCash() {
        return cash;
    }
}
