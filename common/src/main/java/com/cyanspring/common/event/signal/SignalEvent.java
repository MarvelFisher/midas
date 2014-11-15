package com.cyanspring.common.event.signal;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class SignalEvent extends RemoteAsyncEvent {
	private DataObject signal;

	public SignalEvent(String key, String receiver, DataObject signal) {
		super(key, receiver);
		this.signal = signal;
	}

	public DataObject getSignal() {
		return signal;
	}
	
}
