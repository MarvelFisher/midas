package com.cyanspring.common.event.account;

import java.util.Map;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PremiumFollowPositionReplyEvent extends RemoteAsyncEvent {

	private String user;
	private String account;
	private String market;
	private String symbol;
	private String txId;
	private Map<String, OpenPosition> positionMap;
	
	public PremiumFollowPositionReplyEvent(String key, String receiver, String user, String account, String market, Map<String, OpenPosition> positionMap, String symbol, String txId) {
		super(key, receiver);
		this.user = user;
		this.account = account;
		this.market = market;
		this.symbol = symbol;
		this.txId = txId;
		this.positionMap = positionMap;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getTxId() {
		return txId;
	}

	public Map<String, OpenPosition> getPositionMap() {
		return positionMap;
	}

	public String getUser() {
		return user;
	}

	public String getAccount() {
		return account;
	}

	public String getMarket() {
		return market;
	}

}
