package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryOrderAlertRequestEvent extends RemoteAsyncEvent{
	private AlertType type;
	private String txId;
	private String userId;
	/*
	 * 	TRADE_QUERY_OLD(6),
	 * */
	public QueryOrderAlertRequestEvent(String key, String receiver,
			String userId, String txId, AlertType type) {
		super(key, receiver);		
		this.userId = userId;
		this.txId = txId ;
		this.type = type;
	}
	
	public String getTxId() {
		return txId;
	}
	
	public AlertType getType()
	{
		return type ;
	}

	public String getuserId() {
		return userId;
	}
}
