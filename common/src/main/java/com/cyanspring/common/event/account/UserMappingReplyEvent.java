package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingReplyEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String userThirdParty;
    private boolean userExist;
    private boolean userThirdPartyExist;

    public UserMappingReplyEvent(String key, String receiver, String txId, String user, String userThirdParty, boolean userExist, boolean userThirdPartyExist) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
        this.userExist = userExist;
        this.userThirdPartyExist = userThirdPartyExist;
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

    public boolean isUserExist() {
        return userExist;
    }

    public boolean isUserThirdPartyExist() {
        return userThirdPartyExist;
    }
}
