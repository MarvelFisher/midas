package com.cyanspring.common.event.account;

import java.util.Map;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class PremiumFollowPositionReplyEvent extends RemoteAsyncEvent {

	private String symbol;
	private String txId;
	private Map<String, OpenPosition> positionMap;
	
	public PremiumFollowPositionReplyEvent(String key, String receiver, Map<String, OpenPosition> positionMap, String symbol, String txId) {
		super(key, receiver);
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

}
