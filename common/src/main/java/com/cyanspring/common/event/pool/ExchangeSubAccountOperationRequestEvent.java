package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class ExchangeSubAccountOperationRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 1985001345168909542L;

	private ExchangeSubAccount exchangeSubAccount;

	private OperationType operationType;

	public ExchangeSubAccountOperationRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public ExchangeSubAccount getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setExchangeSubAccount(ExchangeSubAccount exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}
}
