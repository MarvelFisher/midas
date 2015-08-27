package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

import java.util.List;

public class QuoteExtSubEvent extends RemoteAsyncEvent{

    private List<String> requestSymbolList;

    public QuoteExtSubEvent(String key, String receiver,List<String> requestSymbolList) {
        super(key, receiver);
        this.requestSymbolList = requestSymbolList;
    }

    public List<String> getRequestSymbolList() {
        return requestSymbolList;
    }
}
