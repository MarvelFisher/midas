package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.alert.PriceAlert;

public class SetPriceAlertRequestEvent extends RemoteAsyncEvent {
	private PriceAlert priceAlert;
	private int Type;
	private String txId;
	/*
	 * SetType : 0 : NEW
	 * SetType : 1 : MODIFY
	 * SetType : 2 : CANCEL
	 * */
	public SetPriceAlertRequestEvent(String key, String receiver,
			PriceAlert priceAlert, String txId, int type) {
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
	public int getType()
	{
		return Type ;
	}
}
