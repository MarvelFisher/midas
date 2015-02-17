package com.cyanspring.common.event.info;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class CentralDbReadyEvent  extends RemoteAsyncEvent
{
	private List<Map<Double, Double>> tickTableList = null;
	public CentralDbReadyEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public List<Map<Double, Double>> getTickTableList() {
		return tickTableList;
	}
	public void setTickTableList(List<Map<Double, Double>> tickTableList) {
		this.tickTableList = tickTableList;
	}
}
