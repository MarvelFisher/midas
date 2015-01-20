package com.cyanspring.common.event;

public interface IAsyncEventBridge {
	String getBridgeId();
	void onBridgeEvent(RemoteAsyncEvent event);
}
