package com.cyanspring.common.event.account;

public class UserTerminateUpdateEvent extends UserTerminateEvent {

    public UserTerminateUpdateEvent(String key, String receiver, String userId, boolean terminate) {
        super(key, receiver, userId, terminate);
    }
}
