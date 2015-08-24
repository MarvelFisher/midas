package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CoinSettingReplyEvent extends RemoteAsyncEvent {

    private static final long serialVersionUID = 1L;
    private String userId;
    private String txId;
    private boolean isOk;
    private String message;

    public CoinSettingReplyEvent(String key, String receiver, String userId, String txId, boolean isOk, String message) {
        super(key, receiver);
        this.txId = txId;
        this.userId = userId;
        this.isOk = isOk;
        this.message = message;
    }

    public String getTxId() {
        return txId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isOk() {
        return isOk;
    }

    public String getMessage() {
        return message;
    }

}
