package com.cyanspring.common.event;

abstract public class AbstractRequestEvent extends RemoteAsyncEvent {
	private String txId;
	
	public AbstractRequestEvent(String key, String receiver, String txId) {
		super(key, receiver);
		this.txId = txId;
	}

	public String getTxId() {
		return txId;
	}
}
