package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.PersistType;
import com.cyanspring.server.persistence.PersistenceManager;

public class SignalRecoveryProcessor implements IRecoveryProcessor<DataObject> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<DataObject> recover() {
		return persistenceManager.recoverObject(PersistType.SIGNAL, false);
	}

}
