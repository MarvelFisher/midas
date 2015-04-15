package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

public class AmendSingleInstrumentStrategyEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;
	private String txId;

	public AmendSingleInstrumentStrategyEvent(String key, String receiver,
											  Map<String, Object> fields,
											  String txId) {
		super(key, receiver);
		this.fields = fields;
		this.txId = txId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

}
