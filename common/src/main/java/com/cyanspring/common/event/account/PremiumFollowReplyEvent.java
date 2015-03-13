package com.cyanspring.common.event.account;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;


public class PremiumFollowReplyEvent extends RemoteAsyncEvent {
	private List<OpenPosition> positions = new ArrayList<OpenPosition>();
	private int error;
	private boolean ok;
	private String message; 
	private String txId;
	
	public PremiumFollowReplyEvent(String key, String receiver,
			List<OpenPosition> positions, int error, boolean ok,
			String message, String txId) {
		super(key, receiver);
		this.positions = positions;
		this.error = error;
		this.ok = ok;
		this.message = message;
		this.txId = txId;
	}
	public List<OpenPosition> getPositions() {
		return positions;
	}
	public void setPositions(List<OpenPosition> positions) {
		this.positions = positions;
	}
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}

	
}
