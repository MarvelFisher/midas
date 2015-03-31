package com.cyanspring.common.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class SendNotificationRequestEvent extends RemoteAsyncEvent{
	private String txId;
	private ParseData PD;	
	
	public SendNotificationRequestEvent(String key, String receiver,
			String txId, ParseData pd) {
		super(key, receiver);
		this.setPD(pd);
		this.setTxId(txId);
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public ParseData getPD() {
		return PD;
	}

	public void setPD(ParseData pD) {
		PD = pD;
	}

}
