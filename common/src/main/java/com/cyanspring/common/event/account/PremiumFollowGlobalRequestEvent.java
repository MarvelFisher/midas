package com.cyanspring.common.event.account;

import com.cyanspring.common.account.PremiumFollowInfo;

public class PremiumFollowGlobalRequestEvent extends PremiumFollowRequestEvent {

	public PremiumFollowGlobalRequestEvent(String key, String receiver, String originSender,
			PremiumFollowInfo info, String userId, String accountId, String txId, String originTxId) {
		super(key, receiver, info, userId, accountId, txId);
		this.originSender = originSender;
		this.originTxId = originTxId;
	}
	 
	private String originSender;
	private String originTxId;
	public String getOriginSender() {
		return originSender;
	} 
	
	public String getOriginTxId(){
		return originTxId;
	}

}
