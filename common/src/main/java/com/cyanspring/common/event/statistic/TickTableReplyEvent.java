package com.cyanspring.common.event.statistic;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.staticdata.AbstractTickTable;
import com.cyanspring.common.staticdata.RefData;

public class TickTableReplyEvent extends RemoteAsyncEvent {
	
	private Map<AbstractTickTable,List<RefData>> map;
	public TickTableReplyEvent(String key, String receiver, Map<AbstractTickTable,List<RefData>> map) {
		super(key, receiver);
		this.map = map;
	}
	public Map<AbstractTickTable,List<RefData>> getMap() {
		return map;
	}
}
