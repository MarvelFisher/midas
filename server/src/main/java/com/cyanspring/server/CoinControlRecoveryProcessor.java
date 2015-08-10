package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.business.CoinControl;
import com.cyanspring.server.persistence.PersistenceManager;

public class CoinControlRecoveryProcessor implements IRecoveryProcessor<CoinControl> {

	@Autowired
	private PersistenceManager persistenceManager;
	
	@Override
	public List<CoinControl> recover() {
		return persistenceManager.recoverCoinControl();
	}

}
