package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseRequestEvent;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class AccountInstrumentSnapshotRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 8355371499405802076L;

	public AccountInstrumentSnapshotRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}
}
