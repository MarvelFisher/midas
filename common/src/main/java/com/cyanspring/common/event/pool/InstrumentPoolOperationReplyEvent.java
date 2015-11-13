package com.cyanspring.common.event.pool;

import com.cyanspring.common.event.BaseReplyEvent;
import com.cyanspring.common.event.OperationType;
import com.cyanspring.common.pool.InstrumentPool;

/**
 * @author GuoWei
 * @since 11/12/2015
 */
public class InstrumentPoolOperationReplyEvent extends BaseReplyEvent {

	private static final long serialVersionUID = 4582453963657538557L;

	private InstrumentPool instrumentPool;

	private OperationType operationType;

	public InstrumentPoolOperationReplyEvent(String key, String receiver,
			boolean ok, String message, int errorCode, String txId) {
		super(key, receiver, ok, message, errorCode, txId);
	}

	public InstrumentPool getInstrumentPool() {
		return instrumentPool;
	}

	public void setInstrumentPool(InstrumentPool instrumentPool) {
		this.instrumentPool = instrumentPool;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
}
