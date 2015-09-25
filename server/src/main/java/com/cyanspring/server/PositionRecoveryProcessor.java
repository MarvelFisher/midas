package com.cyanspring.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.server.persistence.PersistenceManager;

public class PositionRecoveryProcessor {
	@Autowired
	private PersistenceManager persistenceManager;
	
	private boolean todayOnly;

	public List<OpenPosition> recoverOpenPositions() {
		return persistenceManager.recoverOpenPositions();
	}
	
	public List<ClosedPosition> recoverClosedPositions() {
		return persistenceManager.recoverClosedPositions(todayOnly);
	}

	public boolean isTodayOnly() {
		return todayOnly;
	}

	public void setTodayOnly(boolean todayOnly) {
		this.todayOnly = todayOnly;
	}
}
