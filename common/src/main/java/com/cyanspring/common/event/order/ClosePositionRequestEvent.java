package com.cyanspring.common.event.order;

import com.cyanspring.common.account.PositionCloseReason;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClosePositionRequestEvent extends RemoteAsyncEvent {
	private String account;
	private String symbol;
	private PositionCloseReason reason;
	private String txId;
	
	public ClosePositionRequestEvent(String key, String receiver,
			String account, String symbol, PositionCloseReason reason, String txId) {
		super(key, receiver);
		this.account = account;
		this.symbol = symbol;
		this.reason = reason;
		this.txId = txId;
	}

	public String getAccount() {
		return account;
	}

	public String getSymbol() {
		return symbol;
	}

	public PositionCloseReason getReason() {
		return reason;
	}
	
	public String getTxId() {
		return txId;
	}

}
