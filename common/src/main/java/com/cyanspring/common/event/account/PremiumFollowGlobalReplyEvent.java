package com.cyanspring.common.event.account;

import java.util.List;

import com.cyanspring.common.account.OpenPosition;

public class PremiumFollowGlobalReplyEvent extends PremiumFollowReplyEvent {

	public PremiumFollowGlobalReplyEvent(String key, String receiver,
			List<OpenPosition> positions, int error, boolean ok,
			String message, String txId) {
		super(key, receiver, positions, error, ok, message, txId);
	}

}
