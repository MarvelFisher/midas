package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.alert.PriceAlert;

public class CreatePriceAlertRequestEvent extends RemoteAsyncEvent {
	private PriceAlert priceAlert;
	private String txId;
	public CreatePriceAlertRequestEvent(String key, String receiver,
			PriceAlert priceAlert, String txId) {
		super(key, receiver);
		this.priceAlert = priceAlert;
		this.txId = txId;
	}
	public PriceAlert getPriceAlert() {
		return priceAlert;
	}
	public String getTxId() {
		return txId;
	}

	
}
