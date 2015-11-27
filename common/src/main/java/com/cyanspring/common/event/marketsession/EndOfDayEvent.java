package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class EndOfDayEvent extends RemoteAsyncEvent{

	private String tradeDate;
	public EndOfDayEvent(String key, String receiver,String tradeDate) {
		super(key, receiver);
		this.tradeDate = tradeDate;
	}
	/**
	 * @return the tradeDate
	 */
	public String getTradeDate() {
		return tradeDate;
	}
}
