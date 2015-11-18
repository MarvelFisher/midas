package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class ExchangeSubAccountUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = -5552897539668685918L;

	private ExchangeSubAccount exchangeSubAccount;

	private OperationType operationType;

	public ExchangeSubAccountUpdateEvent(String key, String receiver,
			ExchangeSubAccount exchangeSubAccount, OperationType operationType) {
		super(key, receiver);
		this.exchangeSubAccount = exchangeSubAccount;
		this.operationType = operationType;
	}

	public ExchangeSubAccount getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
