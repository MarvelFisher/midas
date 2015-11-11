package com.cyanspring.common.event;

/**
 * @author GuoWei
 * @version 11/10/2015
 */
public class BaseRequestEvent extends RemoteAsyncEvent {

	private static final long serialVersionUID = 6112607894628454755L;
	
	private String txId;

	public BaseRequestEvent(String key, String receiver, String txId) {
		super(key, receiver);
		this.txId = txId;
	}

	public String getTxId() {
		return txId;
	}
}