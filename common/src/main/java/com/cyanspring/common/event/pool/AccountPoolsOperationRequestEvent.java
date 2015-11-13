package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.AccountPool;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class AccountPoolsOperationRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 1448243882834186724L;

	private List<AccountPool> accountPools;

	private OperationType operationType;

	public AccountPoolsOperationRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}

	public List<AccountPool> getAccountPools() {
		return accountPools;
	}

	public void setAccountPools(List<AccountPool> accountPools) {
		this.accountPools = accountPools;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}

}
