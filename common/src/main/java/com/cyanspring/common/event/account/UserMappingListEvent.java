package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingListEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String market;
    private String language;

    public UserMappingListEvent(String key, String receiver, String txId, String user, String market, String language) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.market = market;
        this.language = language;
    }

    public String getTxId() {
        return txId;
    }

    public String getUser() {
        return user;
    }

    public String getMarket() {
        return market;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "UserMappingListEvent{" +
                "txId='" + txId + '\'' +
                ", user='" + user + '\'' +
                ", market='" + market + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
