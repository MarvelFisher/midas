package com.cyanspring.common.event.account;

import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ExecutionUpdateEvent extends RemoteAsyncEvent {
	private Execution execution;

	public ExecutionUpdateEvent(String key, String receiver, Execution execution) {
		super(key, receiver);
		this.execution = execution;
	}

	public Execution getExecution() {
		return execution;
	}
	
}
