package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryOrderAlertRequestEvent extends RemoteAsyncEvent{
	private AlertType Type;
	private String txId;
	private String accountId;
	/*
	 * 	TRADE_QUERY_OLD(6),
	 * */
	public QueryOrderAlertRequestEvent(String key, String receiver,
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
