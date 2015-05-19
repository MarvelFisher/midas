package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserMappingReplyEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String userThirdParty;
    private boolean userExist;
    private boolean userThirdPartyExist;
    private String market;
    private String language;
    private String clientId;
    private boolean transferring;
    private boolean oldThirdPartyUser;

    public UserMappingReplyEvent(String key, String receiver, String txId, String user, String userThirdParty, boolean userExist, boolean userThirdPartyExist, String market, String language, String clientId) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.userThirdParty = userThirdParty;
        this.userExist = userExist;
        this.userThirdPartyExist = userThirdPartyExist;
        this.market = market;
        this.language = language;
        this.clientId = clientId;
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

    public String getClientId() {
        return clientId;
    }

    public String getMarket() {
        return market;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isTransferring() {
        return transferring;
    }

    public void setTransferring(boolean transferring) {
        this.transferring = transferring;
    }

    public boolean isOldThirdPartyUser() {
        return oldThirdPartyUser;
    }

    public void setOldThirdPartyUser(boolean oldThirdPartyUser) {
        this.oldThirdPartyUser = oldThirdPartyUser;
    }

    @Override
    public String toString() {
        return "UserMappingReplyEvent{" +
                "txId='" + txId + '\'' +
                ", user='" + user + '\'' +
                ", userThirdParty='" + userThirdParty + '\'' +
                ", userExist=" + userExist +
                ", userThirdPartyExist=" + userThirdPartyExist +
                ", market='" + market + '\'' +
                ", language='" + language + '\'' +
                ", clientId='" + clientId + '\'' +
                ", transferring=" + transferring +
                ", oldThirdPartyUser=" + oldThirdPartyUser +
                '}';
    }
}
