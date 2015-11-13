package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordUpdateEvent extends BaseReplyEvent {

	private static final long serialVersionUID = -1113113126905620202L;

	private InstrumentPoolRecord instrumentPoolRecord;

	public InstrumentPoolRecordUpdateEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public InstrumentPoolRecord getInstrumentPoolRecord() {
		return instrumentPoolRecord;
	}

	public void setInstrumentPoolRecord(
			InstrumentPoolRecord instrumentPoolRecord) {
		this.instrumentPoolRecord = instrumentPoolRecord;
	}
}
