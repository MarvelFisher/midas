package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class CentralDbSubscribeEvent extends RemoteAsyncEvent 
{
	public CentralDbSubscribeEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
}
