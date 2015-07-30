package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ManualClosePositionRequestEvent extends RemoteAsyncEvent {

	private OpenPosition openPosition;
	private String txId;
	private double price;
	public ManualClosePositionRequestEvent(String key, String receiver, OpenPosition openPosition, String txId, double price) {
		super(key, receiver);
		this.openPosition = openPosition;
		this.txId = txId;
		this.price = price;
	}
	
	public double getPrice() {
		return price;
	}


	public void setPrice(double price) {
		this.price = price;
	}


	public OpenPosition getOpenPosition() {
		return openPosition;
	}

	public void setOpenPosition(OpenPosition openPosition) {
		this.openPosition = openPosition;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}
	
 }
