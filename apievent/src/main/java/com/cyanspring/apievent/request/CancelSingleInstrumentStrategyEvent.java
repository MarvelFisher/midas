package com.cyanspring.apievent.request;

import com.cyanspring.apievent.ClientEvent;

public class CancelSingleInstrumentStrategyEvent extends ClientEvent {
	private String txId;
	
	public CancelSingleInstrumentStrategyEvent(String key, String receiver,
											   String txId) {
		super(key, receiver);
		this.txId = txId;
	}
	public String getTxId() {
		return txId;
	}

}
