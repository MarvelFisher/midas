package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/25/2015
 */
public class UserExchangeSubAccountOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = -8618562202362144793L;

	public UserExchangeSubAccountOperationReplyEvent(String key,
			String receiver, boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
