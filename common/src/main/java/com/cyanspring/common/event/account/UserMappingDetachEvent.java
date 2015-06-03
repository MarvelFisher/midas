package com.cyanspring.common.event.account;

import com.cyanspring.common.account.UserType;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingDetachEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String password;
    private String userThirdParty;
    private String market;
    private String language;
    private boolean isAttach;
    private UserType userType;

    public UserMappingDetachEvent(
            String key, String receiver, String txId, String user, String password, String userThirdParty,
            String market, String language, boolean isAttach, UserType userType) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.password = password;
        this.userThirdParty = userThirdParty;
        this.market = market;
        this.language = language;
        this.isAttach = isAttach;
        this.userType = userType;
        setPriority(EventPriority.HIGH);
    }

    public String getTxId() {
        return txId;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getUserThirdParty() {
        return userThirdParty;
    }

    public String getMarket() {
        return market;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isAttach() {
        return isAttach;
    }

    public UserType getUserType() {
        return userType;
    }

    @Override
    public String toString() {
        return "UserMappingDetachEvent{" +
                "txId='" + txId + '\'' +
                ", user='" + user + '\'' +
                ", userThirdParty='" + userThirdParty + '\'' +
                ", market='" + market + '\'' +
                ", language='" + language + '\'' +
                ", isAttach=" + isAttach +
                ", userType=" + userType +
                '}';
    }
}
