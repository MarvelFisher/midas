package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.pool.InstrumentPoolRecord;
import com.cyanspring.server.persistence.PersistenceManager;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public class InstrumentPoolRecordRecoveryProcessor implements
		IRecoveryProcessor<InstrumentPoolRecord> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<InstrumentPoolRecord> recover() {
		return persistenceManager.recoverAccountPoolRecords();
	}
}
