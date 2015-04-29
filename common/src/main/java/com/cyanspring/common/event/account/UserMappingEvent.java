package com.cyanspring.common.event.account;

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String userThirdParty;
    private String clientId;

    public UserMappingEvent(String key, String receiver, String txId, String user, String userThirdParty, String clientId) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
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
}
