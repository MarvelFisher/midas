package com.cyanspring.common.event.kdb;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.kdb.MarketIntelligence;

import java.util.List;

public class MarketIntelligenceUpdateEvent extends RemoteAsyncEvent {

    private List<MarketIntelligence> indexes;

    public MarketIntelligenceUpdateEvent(String key, String receiver, List<MarketIntelligence> indexes) {
        super(key, receiver);
        this.indexes = indexes;
    }

    public List<MarketIntelligence> getIndexes() {
        return indexes;
    }

    @Override
    public String toString() {
        return "MarketIntelligenceUpdateEvent{" +
                "indexes=" + indexes +
                '}';
    }
}
