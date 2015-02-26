package com.cyanspring.common.event.info;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.ITickTable;

@SuppressWarnings("serial")
public class CentralDbReadyEvent  extends RemoteAsyncEvent
{
	private Map<String, ITickTable> tickTableList = null;
	public CentralDbReadyEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public Map<String, ITickTable> getTickTableList() {
		return tickTableList;
	}
	public void setTickTableList(Map<String, ITickTable> tickTableList) {
		this.tickTableList = tickTableList;
	}
}
