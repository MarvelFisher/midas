package com.cyanspring.common.event.account;

import com.cyanspring.common.account.PremiumFollowInfo;

public class PremiumFollowGlobalRequestEvent extends PremiumFollowRequestEvent {

	public PremiumFollowGlobalRequestEvent(String key, String receiver,
			PremiumFollowInfo info, String txId) {
		super(key, receiver, info, txId);
	}

}
