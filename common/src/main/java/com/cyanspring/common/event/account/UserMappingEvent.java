package com.cyanspring.common.event.account;

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String userThirdParty;
    private String market;
    private String language;
    private String clientId;

    public UserMappingEvent(String key, String receiver, String txId, String user, String userThirdParty, String market, String language, String clientId) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
        this.market = market;
        this.language = language;
        this.clientId = clientId;
        setPriority(EventPriority.HIGH);
    }

    public String getTxId() {
        return txId;
    }

    public String getUser() {
        return user;
    }

    public String getUserThirdParty() {
        return userThirdParty;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMarket() {
        return market;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "UserMappingEvent{" +
                "txId='" + txId + '\'' +
                ", user='" + user + '\'' +
                ", userThirdParty='" + userThirdParty + '\'' +
                ", market='" + market + '\'' +
                ", language='" + language + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
