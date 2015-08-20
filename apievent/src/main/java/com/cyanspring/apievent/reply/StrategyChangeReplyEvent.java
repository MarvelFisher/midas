package com.cyanspring.apievent.reply;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public abstract class StrategyChangeReplyEvent extends RemoteAsyncEvent {
	private String txId;
	private boolean success;
	private String message;
	
	public StrategyChangeReplyEvent(String key, String receiver,
									String txId, boolean success, String message) {
		super(key, receiver);
		this.txId = txId;
		this.success = success;
		this.message = message;
	}

	public String getTxId() {
		return txId;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
	
}
