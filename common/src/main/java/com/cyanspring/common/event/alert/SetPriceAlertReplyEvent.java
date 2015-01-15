package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SetPriceAlertReplyEvent extends RemoteAsyncEvent {
	private String id;
	private String txId;
	private String userId;
	private boolean ok;
	private String message;
	/*
	 * if Success , ok = true ,message = "";
	 * if reject , ok = false ,message = error msg ;
	 * */
	public SetPriceAlertReplyEvent(String key, String receiver,
			String id, String txId, boolean ok, String message) {
		super(key, receiver);
		this.id = id;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
	}
	public String getId(){
		return id ;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
