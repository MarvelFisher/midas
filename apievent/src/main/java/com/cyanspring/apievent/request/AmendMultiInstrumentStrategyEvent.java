package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;
import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class AmendMultiInstrumentStrategyEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;
	private List<Map<String, Object>> instruments;
	private String txId;
	
	public AmendMultiInstrumentStrategyEvent(String key, String receiver,
											 Map<String, Object> fields, List<Map<String, Object>> instruments,
											 String txId) {
		super(key, receiver);
		this.fields = fields;
		this.instruments = instruments;
		this.txId = txId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public List<Map<String, Object>> getInstruments() {
		return instruments;
	}

}
