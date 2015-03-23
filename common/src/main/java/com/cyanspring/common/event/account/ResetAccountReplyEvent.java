package com.cyanspring.common.event.account;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ResetAccountReplyEvent extends RemoteAsyncEvent {
	private String accountId;
	private String txId;
	private String userId;
	private String market;
	private String coinId;
	private boolean ok;
	private ResetAccountReplyType type;
	private String message;
	
	public ResetAccountReplyEvent(String key, String receiver, String account,
			String txId, String userid, String market, String coinid, ResetAccountReplyType type, boolean ok, String message) {
		super(key, receiver);
		this.accountId = account;
		this.txId = txId;
		this.userId = userid;
		this.market = market;
		this.coinId = coinid;
		this.type = type;
		this.ok = ok;
		this.message = message;
	}
	public String getAccount() {
		return accountId;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}
	public ResetAccountReplyType getType() {
		return type;
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
