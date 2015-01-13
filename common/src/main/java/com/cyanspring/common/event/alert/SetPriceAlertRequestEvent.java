package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.alert.PriceAlert;

public class SetPriceAlertRequestEvent extends RemoteAsyncEvent {
	private PriceAlert priceAlert;
	private AlertType Type;
	private String txId;
	/*
	 * 	PRICE_SET_NEW(1),
	 * 	PRICE_SET_MODIFY(2),
	 * 	PRICE_SET_CANCEL(3),
	 * */
	public SetPriceAlertRequestEvent(String key, String receiver,
			PriceAlert priceAlert, String txId, AlertType type) {
		super(key, receiver);		
		this.priceAlert = priceAlert;
		this.txId = txId ;
		this.Type = type;
	}
	
	public PriceAlert getPriceAlert() {
		return priceAlert;
	}
	public String getTxId() {
		return txId;
	}
	public AlertType getType()
	{
		return Type ;
	}
}
