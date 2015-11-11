package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.server.persistence.PersistenceManager;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class ExchangeSubAccountRecoveryProcessor implements
		IRecoveryProcessor<ExchangeSubAccount> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<ExchangeSubAccount> recover() {
		return persistenceManager.recoverExchangeSubAccounts();
	}
}
