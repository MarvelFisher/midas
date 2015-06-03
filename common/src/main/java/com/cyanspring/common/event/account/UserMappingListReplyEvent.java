package com.cyanspring.common.event.account;

import com.cyanspring.common.account.ThirdPartyUser;
import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;

public class UserMappingListReplyEvent extends RemoteAsyncEvent {

    private String txId;
    private String user;
    private String market;
    private String language;
    private List<ThirdPartyUser> thirdPartyUsers;

    public UserMappingListReplyEvent(String key, String receiver, String txId, String user, String market, String language, List<ThirdPartyUser> thirdPartyUsers) {
        super(key, receiver);
        this.txId = txId;
        this.user = user;
        this.market = market;
        this.language = language;
        this.thirdPartyUsers = thirdPartyUsers;
    }

    public String getTxId() {
        return txId;
    }

    public String getUser() {
        return user;
    }

    public String getMarket() {
        return market;
    }

    public String getLanguage() {
        return language;
    }

    public List<ThirdPartyUser> getThirdPartyUsers() {
        return thirdPartyUsers;
    }

    @Override
    public String toString() {
        return "UserMappingListReplyEvent{" +
                "txId='" + txId + '\'' +
                ", user='" + user + '\'' +
                ", market='" + market + '\'' +
                ", language='" + language + '\'' +
                ", thirdPartyUsers=" + thirdPartyUsers +
                '}';
    }
}
