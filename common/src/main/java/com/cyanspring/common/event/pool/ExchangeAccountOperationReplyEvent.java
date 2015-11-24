package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class ExchangeAccountOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 2678193437367372834L;

	public ExchangeAccountOperationReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
