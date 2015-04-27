package com.cyanspring.common.event.account;

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String userThirdParty;

    public UserMappingEvent(String key, String receiver, String txId, String user, String userThirdParty) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
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
}
