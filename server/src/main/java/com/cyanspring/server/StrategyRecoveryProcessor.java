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

	private boolean todayOnly = false;
	private boolean liveTrading = false;

	@Override
	public List<DataObject> recover() {
		List<DataObject> list = new ArrayList<>();
		list.addAll(persistenceManager.recoverObject(PersistType.SINGLE_ORDER_STRATEGY, todayOnly));
		list.addAll(persistenceManager.recoverObject(PersistType.SINGLE_INSTRUMENT_STRATEGY, todayOnly));
		list.addAll(persistenceManager.recoverObject(PersistType.MULTI_INSTRUMENT_STRATEGY, todayOnly));
		if (liveTrading) {
			list.addAll(persistenceManager.recoverChildOrderAudit(todayOnly));
		}
		return list;
	}

	public boolean isTodayOnly() {
		return todayOnly;
	}

	public void setTodayOnly(boolean todayOnly) {
		this.todayOnly = todayOnly;
	}

	public boolean isLiveTrading() {
		return liveTrading;
	}

	public void setLiveTrading(boolean liveTrading) {
		this.liveTrading = liveTrading;
	}

}
