package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.account.User;
import com.cyanspring.server.persistence.PersistenceManager;

public class UserRecoveryProcessor implements IRecoveryProcessor<User> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<User> recover() {
		return persistenceManager.recoverUsers();
	}
}
