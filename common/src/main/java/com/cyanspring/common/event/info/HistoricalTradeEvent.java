package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class HistoricalTradeEvent extends RemoteAsyncEvent {
	private List<Execution> trades;

	public HistoricalTradeEvent(String key, String receiver,
			List<Execution> trades) {
		super(key, receiver);
		this.trades = trades;
	}

	public List<Execution> getTrades() {
		return trades;
	}

	
}
