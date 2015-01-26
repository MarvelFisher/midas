package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.alert.BasePriceAlert;

public class SetPriceAlertRequestEvent extends RemoteAsyncEvent {
	private BasePriceAlert priceAlert;
	private AlertType type;
	private String txId;
	/*
	 * 	PRICE_SET_NEW(1),
	 * 	PRICE_SET_MODIFY(2),
	 * 	PRICE_SET_CANCEL(3),
	 * */
	public SetPriceAlertRequestEvent(String key, String receiver,
			BasePriceAlert priceAlert, String txId, AlertType type) {
		super(key, receiver);		
		this.priceAlert = priceAlert;
		this.txId = txId ;
		this.type = type;
	}
	
	public BasePriceAlert getPriceAlert() {
		return priceAlert;
	}
	public String getTxId() {
		return txId;
	}
	public AlertType getType()
	{
		return type ;
	}
	
	@Override
	public String toString()
	{
		return "Type : " + type.toString() + "," + priceAlert.toString() ;
	}
}
