package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class InstrumentPoolOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 4582453963657538557L;

	public InstrumentPoolOperationReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
