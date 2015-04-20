package com.cyanspring.common.event.account;

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UserTerminateEvent extends RemoteAsyncEvent {

    private String userId;

    private boolean terminate;

    /**
     *
     * @param key
     * @param receiver
     * @param userId
     * @param terminate
     *        true: terminate the user, false: re-enable the user
     */
    public UserTerminateEvent(String key, String receiver, String userId, boolean terminate) {

        super(key, receiver);
        this.userId = userId;
        this.terminate = terminate;
        setPriority(EventPriority.HIGH);
    }

    public String getUserId() {
        return userId;
    }

    public boolean isTerminate() {
        return terminate;
    }
}
