package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseUpdateEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPool;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class InstrumentPoolUpdateEvent extends BaseUpdateEvent {

	private static final long serialVersionUID = -5614245616434712919L;

	private InstrumentPool instrumentPool;

	private OperationType operationType;

	public InstrumentPoolUpdateEvent(String key, String receiver,
			InstrumentPool instrumentPool, OperationType operationType) {
		super(key, receiver);
		this.instrumentPool = instrumentPool;
		this.operationType = operationType;
	}

	public InstrumentPool getInstrumentPool() {
		return instrumentPool;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
