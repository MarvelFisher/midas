package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IRecoveryProcessor;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.server.persistence.PersistenceManager;

public class UserGroupRecoveryProcessor implements IRecoveryProcessor<GroupManagement> {
	@Autowired
	private PersistenceManager persistenceManager;

	@Override
	public List<GroupManagement> recover() {	
		return persistenceManager.recoverGroupManagement();
	}

}
