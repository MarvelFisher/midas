package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordsUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = 4335432748287848732L;

	private List<InstrumentPoolRecord> instrumentPoolRecords;

	private OperationType operationType;

	public InstrumentPoolRecordsUpdateEvent(String key, String receiver,
			List<InstrumentPoolRecord> instrumentPoolRecords,
			OperationType operationType) {
		super(key, receiver);
		this.instrumentPoolRecords = instrumentPoolRecords;
		this.operationType = operationType;
	}

	public List<InstrumentPoolRecord> getInstrumentPoolRecords() {
		return instrumentPoolRecords;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
