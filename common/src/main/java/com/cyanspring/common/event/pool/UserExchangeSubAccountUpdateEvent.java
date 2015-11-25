package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.UserExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/25/2015
 */
public class UserExchangeSubAccountUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = 2952104521256662362L;

	private List<UserExchangeSubAccount> userExchangeSubAccounts;

	private OperationType operationType;

	public UserExchangeSubAccountUpdateEvent(String key, String receiver,
			List<UserExchangeSubAccount> userExchangeSubAccounts,
			OperationType operationType) {
		super(key, receiver);
		this.userExchangeSubAccounts = userExchangeSubAccounts;
		this.operationType = operationType;
	}

	public List<UserExchangeSubAccount> getUserExchangeSubAccounts() {
		return userExchangeSubAccounts;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
