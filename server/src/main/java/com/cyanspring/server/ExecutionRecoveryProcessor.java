package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.business.Execution;
import com.cyanspring.server.persistence.PersistenceManager;

public class ExecutionRecoveryProcessor implements IRecoveryProcessor<Execution> {
	@Autowired
	private PersistenceManager persistenceManager;
	
	@Override
	public List<Execution> recover() {
		return persistenceManager.recoverExecutions();
	}

}
