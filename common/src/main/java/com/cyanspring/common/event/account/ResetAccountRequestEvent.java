package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ResetAccountRequestEvent extends RemoteAsyncEvent {
	private String accountId;
	private String txId;
	private String userId;
	private String market;
	private String coinId;
	
	public ResetAccountRequestEvent(String key, String receiver,
			String accountid, String txId, String userid, String market, String coinid) {
		super(key, receiver);
		this.accountId = accountid;
		this.txId = txId;
		this.userId = userid;
		this.market = market;
		this.coinId = coinid;
	}
	
	public String getAccount() {
		return accountId;
	}
	public String getTxId() {
		return txId;
	}

	public String getUserId() {
		return userId;
	}

	public String getMarket() {
		return market;
	}

	public String getCoinId() {
		return coinId;
	}
}
