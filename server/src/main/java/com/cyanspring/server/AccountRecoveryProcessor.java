package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.account.Account;
import com.cyanspring.server.persistence.PersistenceManager;

public class AccountRecoveryProcessor implements IRecoveryProcessor<Account> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<Account> recover() {
		return persistenceManager.recoverAccounts();
	}
}
