package com.cyanspring.common.event.account;

import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserTerminateReplyEvent extends RemoteAsyncEvent {

    private boolean ok;
    private String message;
    private String userId;
    private TerminationStatus terminationStatus;

    public UserTerminateReplyEvent(String key, String receiver, boolean ok, String message, String userId, TerminationStatus terminationStatus) {
        super(key, receiver);
        this.ok = ok;
        this.message = message;
        this.userId = userId;
        this.terminationStatus = terminationStatus;
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

    public TerminationStatus getTerminationStatus() {
        return terminationStatus;
    }
}
