package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryPriceAlertRequestEvent extends RemoteAsyncEvent{
	private AlertType Type;
	private String txId;
	private String accountId;
	/*
	 * 	PRICE_QUERY_OLD(4),
	 *  PRICE_QUERY_CUR(5),
	 * */
	public QueryPriceAlertRequestEvent(String key, String receiver,
			String accountId, String txId, AlertType type) {
		super(key, receiver);		
		this.accountId = accountId;
		this.txId = txId ;
		this.Type = type;
	}
	
	public String getTxId() {
		return txId;
	}
	public AlertType getType()
	{
		return Type ;
	}
	public String getAccountId() {
		return accountId;
	}
}
