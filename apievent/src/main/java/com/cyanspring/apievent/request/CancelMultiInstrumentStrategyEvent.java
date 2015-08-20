package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class CancelMultiInstrumentStrategyEvent extends RemoteAsyncEvent {
	private String txId;
	
	public CancelMultiInstrumentStrategyEvent(String key, String receiver,
											  String txId) {
		super(key, receiver);
		this.txId = txId;
	}
	public String getTxId() {
		return txId;
	}

}
