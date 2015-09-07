package com.cyanspring.apievent.reply;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class UserLoginReplyEvent extends RemoteAsyncEvent {
    private boolean ok;
    private String account;
    private String message;
    private String txId;

    public UserLoginReplyEvent(String key, String receiver, String account, boolean ok,
                               String message, String txId) {
        super(key, receiver);
        this.account = account;
        this.ok = ok;
        this.message = message;
        this.txId = txId;
    }

    public String getAccount() {
		return account;
	}

	public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public String getTxId() {
        return txId;
    }

}
