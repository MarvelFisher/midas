package com.cyanspring.common.event.marketsession;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class MarketSessionRequestEvent  extends RemoteAsyncEvent {

	private boolean local;
	public MarketSessionRequestEvent(String key, String receiver) {
		super(key, receiver);
		local = false;
	}
	
	public MarketSessionRequestEvent(String key, String receiver, boolean local){
		super(key, receiver);
		this.local = local;
	}

	public boolean isLocal() {
		return local;
	}
	
	

}
