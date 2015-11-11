package com.cyanspring.common.event;

import com.cyanspring.common.ErrorSchema;

/**
 * @author GuoWei
 * @version 11/10/2015
 */
public class BaseReplyEvent extends RemoteAsyncEvent {

	private static final long serialVersionUID = 3069474271691966087L;

	private String message;

	private boolean ok;

	private int errorCode;

	private String txId;

	public BaseReplyEvent(String key, String receiver, boolean ok,
			String message, int errorCode, String txId) {
		super(key, receiver);
		this.message = message;
		this.ok = ok;
		this.txId = txId;
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public boolean isOk() {
		return ok;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getTxId() {
		return txId;
	}

	public String getErrorMessage(String language) {
		if (errorCode == -1) {
			return null;
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append(ErrorSchema.getMsg(this.errorCode, language));
			if (this.message != null && this.message.length() > 0) {
				builder.append(": ");
				builder.append(this.message);
			}
			return builder.toString();
		}
	}
}