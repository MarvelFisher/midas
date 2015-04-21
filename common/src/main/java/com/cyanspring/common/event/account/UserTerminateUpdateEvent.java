package com.cyanspring.common.event.account;

import com.cyanspring.common.account.TerminationStatus;

public class UserTerminateUpdateEvent extends UserTerminateEvent {

    public UserTerminateUpdateEvent(String key, String receiver, String userId, TerminationStatus terminationStatus) {
        super(key, receiver, userId, terminationStatus);
    }
}
