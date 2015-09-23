package com.cyanspring.common.event.statistic;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.AbstractTickTable;

public class TickTableReplyEvent extends RemoteAsyncEvent {
	
	private Map<AbstractTickTable,List<String>> map;
	public TickTableReplyEvent(String key, String receiver, Map<AbstractTickTable,List<String>> map) {
		super(key, receiver);
		this.map = map;
	}
	public Map<AbstractTickTable,List<String>> getMap() {
		return map;
	}
}
