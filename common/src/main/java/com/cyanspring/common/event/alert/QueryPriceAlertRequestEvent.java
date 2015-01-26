package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryPriceAlertRequestEvent extends RemoteAsyncEvent{
	private AlertType type;
	private String txId;
	private String userId;
	/*
	 * 	PRICE_QUERY_OLD(4),
	 *  PRICE_QUERY_CUR(5),
	 * */
	public QueryPriceAlertRequestEvent(String key, String receiver,
			String UserId, String txId, AlertType type) {
		super(key, receiver);		
		this.userId = UserId;
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
	public String getUserId() {
		return userId;
	}
	@Override
	public String toString()
	{
		return "Type : " + type.toString() + ",userId : " + userId ;
	}
}
