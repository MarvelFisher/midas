package com.cyanspring.common.event.account;

import com.cyanspring.common.business.CoinType;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CoinSettingReplyEvent extends RemoteAsyncEvent {

    private static final long serialVersionUID = 1L;
    private String txId;
    private String userId;
	private String clientId;
    private CoinType coinType;
    private String market;
    private boolean isOk;
    private String message;

    public CoinSettingReplyEvent(String key, String receiver, String txId, String userId, String clientId,
                                 CoinType coinType, String market, boolean isOk, String message) {
        super(key, receiver);
        this.txId = txId;
        this.userId = userId;
        this.clientId = clientId;
        this.coinType = coinType;
        this.market = market;
        this.isOk = isOk;
        this.message = message;
    }

    public String getTxId() {
        return txId;
    }

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }

    public CoinType getCoinType() {
        return coinType;
    }

    public String getMarket() {
        return market;
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
                "txId='" + txId + '\'' +
                ", userId='" + userId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", coinType=" + coinType +
                ", market='" + market + '\'' +
                ", isOk=" + isOk +
                ", message='" + message + '\'' +
                '}';
    }
}
