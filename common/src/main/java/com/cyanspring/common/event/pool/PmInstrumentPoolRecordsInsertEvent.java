package com.cyanspring.common.event.pool;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class PmInstrumentPoolRecordsInsertEvent extends AsyncEvent {

	private static final long serialVersionUID = -7180946757081432120L;

	private List<InstrumentPoolRecord> instrumentPoolRecords;

	public PmInstrumentPoolRecordsInsertEvent(
			List<InstrumentPoolRecord> instrumentPoolRecords) {
		this.instrumentPoolRecords = instrumentPoolRecords;
	}

	public List<InstrumentPoolRecord> getInstrumentPoolRecords() {
		return instrumentPoolRecords;
	}

}
