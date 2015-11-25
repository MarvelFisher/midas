package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.UserExchangeSubAccount;

/**
 * @author GuoWei
 * @since 11/25/2015
 */
public class UserExchangeSubAccountOperationRequestEvent extends
		BaseRequestEvent {

	private static final long serialVersionUID = -1198589189764565319L;

	private List<UserExchangeSubAccount> userExchangeSubAccounts;

	private OperationType operationType;

	public UserExchangeSubAccountOperationRequestEvent(String key,
			String receiver, String txId) {
		super(key, receiver, txId);
	}

	public List<UserExchangeSubAccount> getUserExchangeSubAccounts() {
		return userExchangeSubAccounts;
	}

	public void setUserExchangeSubAccounts(
			List<UserExchangeSubAccount> userExchangeSubAccounts) {
		this.userExchangeSubAccounts = userExchangeSubAccounts;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
