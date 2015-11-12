package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class ExchangeSubAccountOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = -6927854481720091350L;

	private ExchangeSubAccount exchangeSubAccount;

	private OperationType operationType;

	public ExchangeSubAccountOperationReplyEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public ExchangeSubAccount getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public void setExchangeSubAccount(ExchangeSubAccount exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
}
