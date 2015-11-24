package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.pool.UserExchangeSubAccount;
import com.cyanspring.server.persistence.PersistenceManager;

public class UserExchangeSubAccountRecoveryProcessor implements
		IRecoveryProcessor<UserExchangeSubAccount> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<UserExchangeSubAccount> recover() {
		return persistenceManager.recoverUserExchangeSubAccounts();
	}

}
