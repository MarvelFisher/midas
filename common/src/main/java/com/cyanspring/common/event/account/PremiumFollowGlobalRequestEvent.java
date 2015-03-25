package com.cyanspring.common.event.account;

import com.cyanspring.common.account.PremiumFollowInfo;

public class PremiumFollowGlobalRequestEvent extends PremiumFollowRequestEvent {

	public PremiumFollowGlobalRequestEvent(String key, String receiver, String orginSender,
			PremiumFollowInfo info, String userId, String accountId, String txId, String orginTxId) {
		super(key, receiver, info, userId, accountId, txId);
		this.orginSender = orginSender;
		this.orginTxId = orginTxId;
	}
	
	private String orginSender;
	private String orginTxId;
	public String getOrginSender() {
		return orginSender;
	}
	
	public String getOrginTxId(){
		return orginTxId;
	}

}
