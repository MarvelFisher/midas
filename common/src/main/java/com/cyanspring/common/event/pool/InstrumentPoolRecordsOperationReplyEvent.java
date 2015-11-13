package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordsOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 1497342099836503057L;

	private List<InstrumentPoolRecord> instrumentPoolRecords;

	private OperationType operationType;

	public InstrumentPoolRecordsOperationReplyEvent(String key,
			String receiver, boolean ok, String message, int errorCode,
			String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public List<InstrumentPoolRecord> getInstrumentPoolRecords() {
		return instrumentPoolRecords;
	}

	public void setInstrumentPoolRecords(
			List<InstrumentPoolRecord> instrumentPoolRecords) {
		this.instrumentPoolRecords = instrumentPoolRecords;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
}
