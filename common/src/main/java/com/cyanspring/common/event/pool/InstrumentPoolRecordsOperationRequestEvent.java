package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordsOperationRequestEvent extends
		BaseRequestEvent {

	private static final long serialVersionUID = 2856805079327890962L;

	private List<InstrumentPoolRecord> instrumentPoolRecords;

	private OperationType operationType;

	public InstrumentPoolRecordsOperationRequestEvent(String key,
			String receiver, String txId) {
		super(key, receiver, txId);
	}

	public List<InstrumentPoolRecord> getInstrumentPoolRecords() {
		return instrumentPoolRecords;
	}

	public void setInstrumentPoolRecords(
			List<InstrumentPoolRecord> instrumentPoolRecords) {
		this.instrumentPoolRecords = instrumentPoolRecords;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}

}
