package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryPriceAlertRquestEvent extends RemoteAsyncEvent{
	private int Type;
	private String txId;
	private String accountId;
	/*
	 * SetType : 0 : QUERY FINISHED PRICE ALERT
	 * SetType : 1 : QUERY CURRENCY PRICE ALERT
	 * */
	public QueryPriceAlertRquestEvent(String key, String receiver,
			String accountId, String txId, int type) {
		super(key, receiver);		
		this.accountId = accountId;
		this.txId = txId ;
		this.Type = type;
	}
	
	public String getTxId() {
		return txId;
	}
	public int getType()
	{
		return Type ;
	}
	public String getAccountId() {
		return accountId;
	}
}
