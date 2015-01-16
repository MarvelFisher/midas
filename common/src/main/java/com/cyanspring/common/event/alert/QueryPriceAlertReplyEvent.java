package com.cyanspring.common.event.alert;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.alert.PriceAlert;

public class QueryPriceAlertReplyEvent extends RemoteAsyncEvent{
	private List<PriceAlert> PriceAlert;
	private String txId;
	private boolean ok;
	private String userId;
	private String message;
	/*
	 * if Success , ok = true ,message = "";
	 * if reject , ok = false ,message = error msg ;
	 * */
	public QueryPriceAlertReplyEvent(String key, String receiver,
			List<PriceAlert> PriceAlert, String txId, boolean ok, String message) {
		super(key, receiver);
		this.PriceAlert = PriceAlert;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
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
	public List<PriceAlert> getPriceAlertList() {
		return PriceAlert;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
