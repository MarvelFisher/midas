package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordsOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1497342099836503057L;

	public InstrumentPoolRecordsOperationReplyEvent(String key,
			String receiver, boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
