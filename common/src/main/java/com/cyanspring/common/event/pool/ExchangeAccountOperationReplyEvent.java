package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.ExchangeAccount;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class ExchangeAccountOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 2678193437367372834L;

	private ExchangeAccount exchangeAccount;

	private OperationType operationType;

	public ExchangeAccountOperationReplyEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public ExchangeAccount getExchangeAccount() {
		return exchangeAccount;
	}

	public void setExchangeAccount(ExchangeAccount exchangeAccount) {
		this.exchangeAccount = exchangeAccount;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
}
