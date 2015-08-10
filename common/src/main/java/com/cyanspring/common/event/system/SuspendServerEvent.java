package com.cyanspring.common.event.system;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SuspendServerEvent extends RemoteAsyncEvent {

	private boolean suspendServer;
	public SuspendServerEvent(String key, String receiver, boolean suspendServer) {
		super(key, receiver);
		this.suspendServer = suspendServer;
	}
	
	public boolean isSuspendServer() {
		return suspendServer;
	}

	public void setSuspendServer(boolean suspendServer) {
		this.suspendServer = suspendServer;
	}
}
