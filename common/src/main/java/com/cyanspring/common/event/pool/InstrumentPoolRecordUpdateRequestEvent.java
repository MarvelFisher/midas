package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordUpdateRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 3540146755190327590L;

	private InstrumentPoolRecord instrumentPoolRecord;

	public InstrumentPoolRecordUpdateRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}

	public InstrumentPoolRecord getInstrumentPoolRecord() {
		return instrumentPoolRecord;
	}

	public void setInstrumentPoolRecord(
			InstrumentPoolRecord instrumentPoolRecord) {
		this.instrumentPoolRecord = instrumentPoolRecord;
	}
}
