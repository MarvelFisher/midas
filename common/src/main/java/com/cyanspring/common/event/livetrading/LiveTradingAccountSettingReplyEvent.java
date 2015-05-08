package com.cyanspring.common.event.livetrading;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class LiveTradingAccountSettingReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L; 
	private boolean isOk ;

	public LiveTradingAccountSettingReplyEvent(String key, String receiver,boolean isOk) {
		super(key, receiver);
		this.isOk = isOk;
	}

	public boolean isOk() {
		return isOk;
	}
	
}
