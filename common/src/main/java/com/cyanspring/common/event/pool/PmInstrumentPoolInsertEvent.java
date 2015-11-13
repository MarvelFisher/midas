package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.pool.InstrumentPool;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class PmInstrumentPoolInsertEvent extends AsyncEvent {

	private static final long serialVersionUID = 4943389370271631187L;

	private InstrumentPool instrumentPool;

	public PmInstrumentPoolInsertEvent(InstrumentPool instrumentPool) {
		this.instrumentPool = instrumentPool;
	}

	public InstrumentPool getInstrumentPool() {
		return instrumentPool;
	}
}
