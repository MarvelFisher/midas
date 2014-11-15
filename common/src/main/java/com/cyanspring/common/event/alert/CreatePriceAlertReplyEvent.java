package com.cyanspring.common.event.alert;

import com.cyanspring.common.alert.PriceAlert;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CreatePriceAlertReplyEvent extends RemoteAsyncEvent {
	private PriceAlert priceAlert;
	private String txId;
	private boolean ok;
	private String message;
	public CreatePriceAlertReplyEvent(String key, String receiver,
			PriceAlert priceAlert, String txId, boolean ok, String message) {
		super(key, receiver);
		this.priceAlert = priceAlert;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
	}
	public PriceAlert getPriceAlert() {
		return priceAlert;
	}
	public String getTxId() {
		return txId;
	}
	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}

}
