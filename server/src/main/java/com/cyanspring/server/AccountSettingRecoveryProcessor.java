package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.server.persistence.PersistenceManager;

public class AccountSettingRecoveryProcessor implements IRecoveryProcessor<AccountSetting> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<AccountSetting> recover() {
		return persistenceManager.recoverAccountSettings();
	}

}
