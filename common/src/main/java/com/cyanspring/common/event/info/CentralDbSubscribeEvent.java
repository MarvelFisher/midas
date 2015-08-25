package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class CentralDbSubscribeEvent extends RemoteAsyncEvent 
{
	private List<String> indexList = null;
	public CentralDbSubscribeEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public List<String> getIndexList()
	{
		return indexList;
	}
	public void setIndexList(List<String> indexList)
	{
		this.indexList = indexList;
	}
}
