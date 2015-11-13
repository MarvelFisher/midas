package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.InstrumentPool;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class PmInstrumentPoolDeleteEvent extends AsyncEvent {

	private static final long serialVersionUID = -3320303573545812041L;

	private InstrumentPool instrumentPool;

	public PmInstrumentPoolDeleteEvent(InstrumentPool instrumentPool) {
		this.instrumentPool = instrumentPool;
	}

	public InstrumentPool getInstrumentPool() {
		return instrumentPool;
	}
}
