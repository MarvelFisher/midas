package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class AccountInstrumentSnapshotReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1303548411351081003L;

	public AccountInstrumentSnapshotReplyEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}
}
