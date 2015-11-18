package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class AccountPoolsOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1667915342709946860L;

	public AccountPoolsOperationReplyEvent(String key, String receiver,
			boolean ok, String message, String txId) {
		super(key, receiver, ok, message, txId);
	}
}
