package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PmEndOfDayRollEvent extends RemoteAsyncEvent {
	
	private String tradeDateTime;
	public PmEndOfDayRollEvent(String key, String receiver, String tradeDateTime) {
		super(key, receiver);
		this.tradeDateTime = tradeDateTime;
	}
	
	public String getTradeDateTime() {
		return tradeDateTime;
	}
}
