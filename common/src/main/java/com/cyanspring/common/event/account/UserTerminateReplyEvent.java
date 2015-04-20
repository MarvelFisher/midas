package com.cyanspring.common.event.account;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserTerminateReplyEvent extends RemoteAsyncEvent {

    private boolean ok;
    private String message;
    private String userId;
    private boolean terminate;

    public UserTerminateReplyEvent(String key, String receiver, boolean ok, String message, String userId, boolean terminate) {
        super(key, receiver);
        this.ok = ok;
        this.message = message;
        this.userId = userId;
        this.terminate = terminate;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isTerminate() {
        return terminate;
    }
}
