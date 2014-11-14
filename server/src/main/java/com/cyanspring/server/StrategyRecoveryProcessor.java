package com.cyanspring.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.PersistType;
import com.cyanspring.server.persistence.PersistenceManager;

public class StrategyRecoveryProcessor implements IRecoveryProcessor<DataObject> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<DataObject> recover() {
		List<DataObject> list = new ArrayList<DataObject>();
		list.addAll(persistenceManager.recoverObject(PersistType.SINGLE_ORDER_STRATEGY));
		list.addAll(persistenceManager.recoverObject(PersistType.SINGLE_INSTRUMENT_STRATEGY));
		list.addAll(persistenceManager.recoverObject(PersistType.MULTI_INSTRUMENT_STRATEGY));
		return list;
	}

}
