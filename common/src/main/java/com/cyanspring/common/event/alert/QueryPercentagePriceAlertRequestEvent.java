package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class QueryPercentagePriceAlertRequestEvent extends RemoteAsyncEvent{
	private AlertType type;
	private String symbol;
	private String txId;
	private String userId;
	
	public QueryPercentagePriceAlertRequestEvent(String key, String receiver,
			String UserId, String symbol, String txId, AlertType type) {
		super(key, receiver);		
		this.setUserId(UserId);
		this.setSymbol(symbol);
		this.setTxId(txId) ;
		this.setType(type);
	}

	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Override
	public String toString()
	{
		return "Type : " + type.toString() + ",userId : " + userId + ",symbol : " + symbol;
	}
}
