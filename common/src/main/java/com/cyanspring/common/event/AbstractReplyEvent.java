package com.cyanspring.common.event;

public class AbstractReplyEvent extends RemoteAsyncEvent {
	private boolean ok;
	private String txId;
	private String msg;
	
	public AbstractReplyEvent(String key, String receiver, boolean ok,
			String txId, String msg) {
		super(key, receiver);
		this.ok = ok;
		this.txId = txId;
		this.msg = msg;
	}

	public boolean isOk() {
		return ok;
	}

	public String getTxId() {
		return txId;
	}

	public String getMsg() {
		return msg;
	}

}
