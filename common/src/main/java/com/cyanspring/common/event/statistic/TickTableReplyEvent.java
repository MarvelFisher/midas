package com.cyanspring.common.event.statistic;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.AbstractTickTable;

public class TickTableReplyEvent extends RemoteAsyncEvent {
	
	private Map<String, AbstractTickTable> map;
	public TickTableReplyEvent(String key, String receiver, Map<String, AbstractTickTable> map) {
		super(key, receiver);
		this.map = map;
	}
	public Map<String, AbstractTickTable> getMap() {
		return map;
	}
}
