package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class ExchangeSubAccountOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = -6927854481720091350L;

	public ExchangeSubAccountOperationReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
