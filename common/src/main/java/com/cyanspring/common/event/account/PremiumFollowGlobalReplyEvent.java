package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.OpenPosition;

public class PremiumFollowGlobalReplyEvent extends PremiumFollowReplyEvent {

	public PremiumFollowGlobalReplyEvent(String key, String receiver,
			Account account, List<OpenPosition> positions, int error,
			boolean ok, String message, String userId, String accountId, String txId) {
		super(key, receiver, account, positions, error, ok, message, userId, accountId, txId);
	}

}
