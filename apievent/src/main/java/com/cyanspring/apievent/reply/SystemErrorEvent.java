package com.cyanspring.apievent.reply;

import com.cyanspring.common.event.RemoteAsyncEvent;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class SystemErrorEvent extends RemoteAsyncEvent {
    private int errorCode;
    private String message;

    public SystemErrorEvent(String key, String receiver, int errorCode,
                            String message) {
        super(key, receiver);
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
