package com.cyanspring.apievent.reply;

import com.cyanspring.apievent.obj.Order;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public final class EnterParentOrderReplyEvent extends ParentOrderReplyEvent {

	private String user;
	private String account;
	
	public EnterParentOrderReplyEvent(String key, String receiver, boolean ok,
									  String message, String txId, Order order, String user, String account) {
		super(key, receiver, ok, message, txId, order);
		
		this.user = user;
		this.account = account;
	}

	public String getUser()
	{
		return this.user;
	}
	
	public String getAccount()
	{
		return this.account;
	}
}
