package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PremiumFollowPositionRequestEvent extends RemoteAsyncEvent {

	private List<String> fdUsers;	
	private String symbol;
	private String txId;
	public PremiumFollowPositionRequestEvent(String key, String receiver, List<String> fdUsers, String symbol, String txId) {
		super(key, receiver);
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

}
