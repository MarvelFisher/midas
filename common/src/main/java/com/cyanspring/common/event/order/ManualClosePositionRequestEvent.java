package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ManualClosePositionRequestEvent extends RemoteAsyncEvent {

	private OpenPosition openPosition;
	private String txId;
	public ManualClosePositionRequestEvent(String key, String receiver, OpenPosition openPosition, String txId) {
		super(key, receiver);
		this.openPosition = openPosition;
		this.txId = txId;
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
