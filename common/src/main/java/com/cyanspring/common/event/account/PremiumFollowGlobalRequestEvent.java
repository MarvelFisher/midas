package com.cyanspring.common.event.account;

import com.cyanspring.common.account.PremiumFollowInfo;

public class PremiumFollowGlobalRequestEvent extends PremiumFollowRequestEvent {

	public PremiumFollowGlobalRequestEvent(String key, String receiver, String orginSender,
			PremiumFollowInfo info, String userId, String accountId, String txId) {
		super(key, receiver, info, userId, accountId, txId);
		this.orginSender = orginSender;
	}
	
	private String orginSender;
	public String getOrginSender() {
		return orginSender;
	}

}
