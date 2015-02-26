package com.cyanspring.common.event.info;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class CentralDbReadyEvent  extends RemoteAsyncEvent
{
	private HashMap<String, Map<Double, Double>> tickTableList = null;
	public CentralDbReadyEvent(String key, String receiver) 
	{
		super(key, receiver);
	}
	public HashMap<String, Map<Double, Double>> getTickTableList() {
		return tickTableList;
	}
	public void setTickTableList(HashMap<String, Map<Double, Double>> tickTableList) {
		this.tickTableList = tickTableList;
	}
}
