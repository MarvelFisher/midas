package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class EnterParentOrderEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;
	private String txId;
	private boolean fix;
	
	public EnterParentOrderEvent(String key, String receiver,
								 Map<String, Object> fields, String txId, boolean fix) {
		super(key, receiver);
		this.fields = fields;
		this.txId = txId;
		this.fix = fix;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public boolean isFix() {
		return fix;
	}
}
