package com.cyanspring.common.event.account;

import com.cyanspring.common.account.TerminationStatus;
import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserTerminateEvent extends RemoteAsyncEvent {

    private String userId;

    private TerminationStatus terminationStatus;

    public UserTerminateEvent(String key, String receiver, String userId, TerminationStatus terminationStatus) {

        super(key, receiver);
        this.userId = userId;
        this.terminationStatus = terminationStatus;
        setPriority(EventPriority.HIGH);
    }

    public String getUserId() {
        return userId;
    }

    public TerminationStatus getTerminationStatus() {
        return terminationStatus;
    }
}
