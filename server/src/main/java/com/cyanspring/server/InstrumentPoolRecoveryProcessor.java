package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.pool.InstrumentPool;
import com.cyanspring.server.persistence.PersistenceManager;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class InstrumentPoolRecoveryProcessor implements
		IRecoveryProcessor<InstrumentPool> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<InstrumentPool> recover() {
		return persistenceManager.recoverInstrumentPools();
	}
}
