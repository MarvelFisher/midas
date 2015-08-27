package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CoinSettingReplyEvent extends RemoteAsyncEvent {

    private static final long serialVersionUID = 1L;
    private String userId;
    private String txId;
	private String clientId;
    private boolean isOk;
    private String message;

    public CoinSettingReplyEvent(String key, String receiver, String userId, String txId, String clientId, boolean isOk, String message) {
        super(key, receiver);
        this.txId = txId;
        this.userId = userId;
        this.clientId = clientId;
        this.isOk = isOk;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getTxId() {
        return txId;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isOk() {
        return isOk;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CoinSettingReplyEvent{" +
                "userId='" + userId + '\'' +
                ", txId='" + txId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", isOk=" + isOk +
                ", message='" + message + '\'' +
                '}';
    }
}
