package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class TradeDateEvent   extends RemoteAsyncEvent{

	private String tradeDate;
	public TradeDateEvent(String key, String receiver, String tradeDate) {
		super(key, receiver);
		this.tradeDate = tradeDate;
	}
	
	public String getTradeDate(){
		return tradeDate;
	}

}
