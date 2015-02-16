package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class CentralDbReadyEvent  extends RemoteAsyncEvent
{
	public CentralDbReadyEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
}
