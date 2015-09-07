package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class AmendParentOrderEvent extends RemoteAsyncEvent {
	private String id;
	private Map<String, Object> fields;
	private String txId;
	
	public AmendParentOrderEvent(String key, String receiver, String id,
								 Map<String, Object> fields, String txId) {
		super(key, receiver);
		this.id = id;
		this.fields = fields;
		this.txId = txId;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}
	
}
