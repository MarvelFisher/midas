package com.cyanspring.apievent.request;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;
import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */

public class NewMultiInstrumentStrategyEvent extends RemoteAsyncEvent {
    private String txId;
    private Map<String, Object> strategy;
    private List<Map<String, Object>> instruments;

    public NewMultiInstrumentStrategyEvent(String key, String receiver,
                                           Map<String, Object> strategy, List<Map<String, Object>> instruments, String txId) {
        super(key, receiver);
        this.strategy = strategy;
        this.instruments = instruments;
        this.txId = txId + "T";
    }

    public Map<String, Object> getStrategy() {
        return strategy;
    }

    public List<Map<String, Object>> getInstruments() {
        return instruments;
    }

    public String getTxId() {
        return txId;
    }
}
