package com.cyanspring.common.event.account;

import com.cyanspring.common.account.UserType;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingDetachReplyEvent extends RemoteAsyncEvent {

    private boolean ok;
    private String message;
    private String txId;
    private String user;
    private String userThirdParty;
    private String market;
    private String language;
    private boolean isAttach;
    private UserType userType;

    public UserMappingDetachReplyEvent(
            String key, String receiver, boolean ok, String message, String txId, String user, String userThirdParty,
            String market, String language, boolean isAttach, UserType userType) {
        super(key, receiver);
        this.ok = ok;
        this.message = message;
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
        this.market = market;
        this.language = language;
        this.isAttach = isAttach;
        this.userType = userType;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
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
}
