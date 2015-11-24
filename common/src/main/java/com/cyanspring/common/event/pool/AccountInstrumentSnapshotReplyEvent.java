package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.pool.InstrumentPoolKeeper;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class AccountInstrumentSnapshotReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1303548411351081003L;

	private InstrumentPoolKeeper instrumentPoolKeeper;

	public AccountInstrumentSnapshotReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}

	public InstrumentPoolKeeper getInstrumentPoolKeeper() {
		return instrumentPoolKeeper;
	}

	public void setInstrumentPoolKeeper(
			InstrumentPoolKeeper instrumentPoolKeeper) {
		this.instrumentPoolKeeper = instrumentPoolKeeper;
	}
}
