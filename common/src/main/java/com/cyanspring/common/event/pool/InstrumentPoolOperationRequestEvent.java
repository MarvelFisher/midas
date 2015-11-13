package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseRequestEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPool;

/**
 * @author GuoWei
 * @since 11/13/2015
 */
public class InstrumentPoolOperationRequestEvent extends BaseRequestEvent {

	private static final long serialVersionUID = 121314526959396992L;

	private InstrumentPool instrumentPool;

	private OperationType operationType;

	public InstrumentPoolOperationRequestEvent(String key, String receiver,
			String txId) {
		super(key, receiver, txId);
	}

	public InstrumentPool getInstrumentPool() {
		return instrumentPool;
	}

	public void setInstrumentPool(InstrumentPool instrumentPool) {
		this.instrumentPool = instrumentPool;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}

}
