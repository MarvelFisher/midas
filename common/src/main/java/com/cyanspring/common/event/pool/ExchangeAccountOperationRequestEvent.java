package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class ExchangeAccountOperationRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 4771578308859519491L;

	private ExchangeAccount exchangeAccount;

	private OperationType operationType;

	public ExchangeAccountOperationRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public ExchangeAccount getExchangeAccount() {
		return exchangeAccount;
	}

	public void setExchangeAccount(ExchangeAccount exchangeAccount) {
		this.exchangeAccount = exchangeAccount;
	}
}
