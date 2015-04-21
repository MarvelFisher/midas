package com.cyanspring.common.account;

import java.util.HashMap;
import java.util.Map;

public enum TerminationStatus {

    NOT_TERMINATED(0),
    TERMINATED(1),
    BLACK_LISTED(2);

    private final int value;

    TerminationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isTerminated() {
        return !this.equals(NOT_TERMINATED);
    }

    static Map<Integer, TerminationStatus> map = new HashMap<>();
    static {
        for (TerminationStatus status : values()) {
            map.put(status.getValue(), status);
        }
    }

    public static TerminationStatus fromInt(int value){
        return map.get(value);
    }
}
