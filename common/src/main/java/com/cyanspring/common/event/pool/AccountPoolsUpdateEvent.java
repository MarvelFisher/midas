package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.AccountPool;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class AccountPoolsUpdateEvent extends BaseUpdateEvent {
	private static final long serialVersionUID = 8797704661149661035L;

	private List<AccountPool> accountPools;

	private OperationType operationType;

	public AccountPoolsUpdateEvent(String key, String receiver,
			List<AccountPool> accountPools, OperationType operationType) {
		super(key, receiver);
		this.accountPools = accountPools;
		this.operationType = operationType;
	}

	public List<AccountPool> getAccountPools() {
		return accountPools;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
