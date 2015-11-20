package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class GlobalSettingRequestEvent extends RemoteAsyncEvent{
	
	public GlobalSettingRequestEvent(String key, String receiver) {
		super(key, receiver);
	}
}
