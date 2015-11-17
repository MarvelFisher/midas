package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordUpdateReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = -1939138136090880874L;

	public InstrumentPoolRecordUpdateReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
