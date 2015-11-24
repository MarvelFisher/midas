package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolRecordUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = -1113113126905620202L;

	private InstrumentPoolRecord instrumentPoolRecord;

	public InstrumentPoolRecordUpdateEvent(String key, String receiver,
			InstrumentPoolRecord instrumentPoolRecord) {
		super(key, receiver);
		this.instrumentPoolRecord = instrumentPoolRecord;
	}

	public InstrumentPoolRecord getInstrumentPoolRecord() {
		return instrumentPoolRecord;
	}
}
