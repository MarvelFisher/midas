package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class PmInstrumentPoolRecordUpdateEvent extends AsyncEvent {

	private static final long serialVersionUID = 502575144380277655L;

	private InstrumentPoolRecord instrumentPoolRecord;

	public PmInstrumentPoolRecordUpdateEvent(
			InstrumentPoolRecord instrumentPoolRecord) {
		this.instrumentPoolRecord = instrumentPoolRecord;
	}

	public InstrumentPoolRecord getInstrumentPoolRecord() {
		return instrumentPoolRecord;
	}
}
