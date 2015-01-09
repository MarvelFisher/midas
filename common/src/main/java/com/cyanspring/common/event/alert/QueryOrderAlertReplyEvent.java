package com.cyanspring.common.event.alert;

import java.util.List;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.business.Execution;

public class QueryOrderAlertReplyEvent extends RemoteAsyncEvent{
	private List<Execution> trades;
	private String txId;
	private boolean ok;
	private String message;
	/*
	 * if Success , ok = true ,message = "";
	 * if reject , ok = false ,message = error msg ;
	 * */
	public QueryOrderAlertReplyEvent(String key, String receiver,
			List<Execution> trades, String txId, boolean ok, String message) {
		super(key, receiver);
		this.trades = trades;
		this.txId = txId;
		this.ok = ok;
		this.message = message;
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

	public List<Execution> getTrades() {
		return trades;
	}
}
