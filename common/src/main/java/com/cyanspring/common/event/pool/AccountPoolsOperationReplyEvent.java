package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.AccountPool;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class AccountPoolsOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1667915342709946860L;

	private List<AccountPool> accountPools;

	private OperationType operationType;

	public AccountPoolsOperationReplyEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public List<AccountPool> getAccountPools() {
		return accountPools;
	}

	public void setAccountPools(List<AccountPool> accountPools) {
		this.accountPools = accountPools;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
}
