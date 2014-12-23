package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class HistoricalTradeRequestEvent extends RemoteAsyncEvent {
	private String account;

	public HistoricalTradeRequestEvent(String key, String receiver,
			String account) {
		super(key, receiver);
		this.account = account;
	}

	public String getAccount() {
		return account;
	}

}
