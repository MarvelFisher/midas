package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PremiumFollowPositionRequestEvent extends RemoteAsyncEvent {

	private String reqUser;
	private String reqAccount;
	private String market;
	private List<String> fdUsers;	
	private String symbol;
	private String txId;
	public PremiumFollowPositionRequestEvent(String key, String receiver, String reqUser, String reqAccount, String market, List<String> fdUsers, String symbol, String txId) {
		super(key, receiver);
		this.reqUser = reqUser;
		this.reqAccount = reqAccount;
		this.market = market;
		this.fdUsers = fdUsers;
		this.symbol = symbol;
		this.txId = txId;
	}
	public List<String> getFdUsers() {
		return fdUsers;
	}
	public String getSymbol() {
		return symbol;
	}
	public String getTxId() {
		return txId;
	}
	public String getReqUser() {
		return reqUser;
	}
	public String getReqAccount() {
		return reqAccount;
	}
	public String getMarket() {
		return market;
	}

}
