package com.cyanspring.server.order;

public class SuspendSystemController {
	private volatile boolean suspendSystem = false;

	public boolean isSuspendSystem() {
		return suspendSystem;
	}

	public void setSuspendSystem(boolean suspendSystem) {
		this.suspendSystem = suspendSystem;
	}
}
