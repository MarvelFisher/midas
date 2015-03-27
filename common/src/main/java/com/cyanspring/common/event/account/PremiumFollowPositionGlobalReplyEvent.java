package com.cyanspring.common.event.account;

import java.util.Map;

import com.cyanspring.common.account.OpenPosition;

public class PremiumFollowPositionGlobalReplyEvent extends PremiumFollowPositionReplyEvent{
	 
	private String originSender;
	private String originTxId;
	
	public PremiumFollowPositionGlobalReplyEvent(String key, String receiver, String originSender, String user, String account, String market,
			Map<String, OpenPosition> positionMap, String symbol, String txId, String originTxId) {
		super(key, receiver, user, account, market, positionMap, symbol, txId);
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
