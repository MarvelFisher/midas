package com.cyanspring.apievent.request;

import com.cyanspring.apievent.ClientEvent;

import java.util.Map;

public class NewSingleInstrumentStrategyEvent extends ClientEvent {
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
