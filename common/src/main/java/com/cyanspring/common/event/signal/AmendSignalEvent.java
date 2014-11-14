package com.cyanspring.common.event.signal;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("rawtypes")
public class AmendSignalEvent extends RemoteAsyncEvent {
	private Map<String, Object> changes;
	private Map<String, Class> types;
	
	public AmendSignalEvent(String key, String receiver,
			Map<String, Object> changes, Map<String, Class> types) {
		super(key, receiver);
		this.changes = changes;
		this.types = types;
	}

	public Map<String, Object> getChanges() {
		return changes;
	}

	public Map<String, Class> getTypes() {
		return types;
	}
	
}
