package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class RetrieveChartEvent extends RemoteAsyncEvent 
{
	private List<String> symbolList;
	public RetrieveChartEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public List<String> getSymbolList() {
		return symbolList;
	}
	public void setSymbolList(List<String> symbolList) {
		this.symbolList = symbolList;
	}
}
