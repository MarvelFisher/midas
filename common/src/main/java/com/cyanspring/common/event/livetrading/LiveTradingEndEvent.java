package com.cyanspring.common.event.livetrading;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class LiveTradingEndEvent extends RemoteAsyncEvent{
	
	private boolean isNeedClearOrderPosition;
	
	public LiveTradingEndEvent(String key, String receiver,boolean isNeedClearOrderPostion) {
		super(key, receiver);
		this.isNeedClearOrderPosition = isNeedClearOrderPostion;
	}
	
	public boolean isNeedClearOrderPosition() {
		return isNeedClearOrderPosition;
	}
}
