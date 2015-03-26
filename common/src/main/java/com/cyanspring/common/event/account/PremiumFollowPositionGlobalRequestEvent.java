package com.cyanspring.common.event.account;

import java.util.List;

public class PremiumFollowPositionGlobalRequestEvent extends PremiumFollowPositionRequestEvent{

	private String originSender;
	private String originTxId;
	public PremiumFollowPositionGlobalRequestEvent(String key, String receiver, String originSender,
			List<String> fdUser, String symbol, String txId, String originTxId) {
		super(key, receiver, fdUser, symbol, txId);
		this.originSender = originSender;
		this.originTxId = originTxId;
	}
	public String getOriginSender() {
		return originSender;
	}
	public String getOriginTxId() {
		return originTxId;
	}

}
