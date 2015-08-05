package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CoinSettingReplyEvent extends RemoteAsyncEvent{

	private static final long serialVersionUID = 1L;
	private String txId;
	private boolean isOk;
	private String message;
	public CoinSettingReplyEvent(String key, String receiver,String txId,boolean isOk,String message) {
		super(key, receiver);
		this.txId = txId;
		this.isOk = isOk;
		this.message = message;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isOk() {
		return isOk;
	}
	public String getMessage() {
		return message;
	}

}
