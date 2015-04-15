package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

public class NewSingleInstrumentStrategyEvent extends RemoteAsyncEvent {
	private String txId;
	private Map<String, Object> instrument;
	public NewSingleInstrumentStrategyEvent(String key, String receiver,
											String txId, Map<String, Object> instrument) {
		super(key, receiver);
		this.txId = txId;
		this.instrument = instrument;
	}
	public String getTxId() {
		return txId;
	}
	public Map<String, Object> getInstrument() {
		return instrument;
	}

}
