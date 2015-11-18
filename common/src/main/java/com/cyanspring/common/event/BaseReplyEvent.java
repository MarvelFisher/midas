package com.cyanspring.common.event;

/**
 * @author GuoWei
 * @version 11/10/2015
 */
public class BaseReplyEvent extends RemoteAsyncEvent {

	private static final long serialVersionUID = 3069474271691966087L;

	private boolean ok;

	private String errorMessage;

	private String txId;

	public BaseReplyEvent(String key, String receiver, boolean ok,
			String errorMessage, String txId) {
		super(key, receiver);
		this.errorMessage = errorMessage;
		this.ok = ok;
		this.txId = txId;
	}

	public boolean isOk() {
		return ok;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getTxId() {
		return txId;
	}
}