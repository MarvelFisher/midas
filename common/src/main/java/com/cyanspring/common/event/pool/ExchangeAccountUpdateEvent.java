package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class ExchangeAccountUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = -8417552382603517987L;

	private ExchangeAccount exchangeAccount;

	private OperationType operationType;

	public ExchangeAccountUpdateEvent(String key, String receiver,
			ExchangeAccount exchangeAccount, OperationType operationType) {
		super(key, receiver);
		this.exchangeAccount = exchangeAccount;
		this.operationType = operationType;
	}

	public ExchangeAccount getExchangeAccount() {
		return exchangeAccount;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
