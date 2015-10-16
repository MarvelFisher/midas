package com.cyanspring.common.marketdata;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.QtyPrice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The lite market data receiver
 *
 * @author elviswu
 */
public class DataReceiver implements IPlugin, IMarketDataListener {

    private Map<String, IQuoteListener> listeners;   // id, listener
    private Map<String, List<String>> subList = new HashMap<>(); // symbol, listeners
    private List<IMarketDataAdaptor> adaptors = new ArrayList<IMarketDataAdaptor>();
    private Map<String, InnerQuote> quotes = new ConcurrentHashMap<>();
    private Map<String, String> subToLts = new HashMap<>();
    private Map<String, String> ltsToSub = new HashMap<>();
    private List<RefData> refDataList;

    public Quote subscribeSymbol(String id, String symbol) throws MarketDataException {
        for (IMarketDataAdaptor adaptor : adaptors)
            adaptor.subscribeMarketData(getSubscribeSymbol(symbol), this);
        List<String> subs = subList.get(symbol);
        if (subs == null) {
            subs = new ArrayList<>();
            subList.put(symbol, subs);
        }
        if (!subs.contains(id))
            subs.add(id);
        if (quotes.containsKey(symbol))
            return quotes.get(symbol).getQuote();
        return new Quote(symbol, new ArrayList<QtyPrice>(), new ArrayList<QtyPrice>());
    }

    public void unsubscribeSymbol(String id, String symbol) {
        for (IMarketDataAdaptor adaptor : adaptors) {
            adaptor.unsubscribeMarketData(getSubscribeSymbol(symbol), this);
        }
        List<String> subs = subList.get(symbol);
        if (subs != null && subs.contains(id))
            subs.remove(id);
    }

    @Override
    public void onQuote(InnerQuote innerQuote) {
        Quote quote = innerQuote.getQuote();
        String symbol = quote.getSymbol();
        List<String> subListener = subList.get(symbol);
        for (String sub : subListener) {
            IQuoteListener listener = listeners.get(sub);
            listener.onQuote(innerQuote);
        }
        quotes.put(symbol, innerQuote);
    }

    @Override
    public void onQuoteExt(DataObject quoteExt, QuoteSource quoteSource) {
        String symbol = quoteExt.get(String.class, QuoteExtDataField.SYMBOL.toString());
        List<String> subListener = subList.get(symbol);
        for (String sub : subListener) {
            IQuoteListener listener = listeners.get(sub);
            listener.onQuoteExt(quoteExt, quoteSource);
        }
    }

    @Override
    public void onTrade(Trade trade) {
        String symbol = trade.getSymbol();
        List<String> subListener = subList.get(symbol);
        for (String sub : subListener) {
            IQuoteListener listener = listeners.get(sub);
            listener.onTrade(trade);
        }
    }

    public void addQuoteListener(IQuoteListener listener) {
        if (listeners == null)
            listeners = new HashMap<>();
        listeners.put(listener.getId(), listener);
    }

    @Override
    public void init() throws Exception {
        if (refDataList == null)
            throw new Exception("RefData not set");
        for (RefData refData : refDataList) {
            subToLts.put(refData.getCode(), refData.getSymbol());
            ltsToSub.put(refData.getSymbol(), refData.getCode());
        }

    }

    @Override
    public void uninit() {
        for (IMarketDataAdaptor adaptor : adaptors)
            adaptor.uninit();
    }

    public void setRefDataList(List<RefData> refDataList) {
        this.refDataList = refDataList;
    }

    private String getSubscribeSymbol(String symbol) {
        return ltsToSub.get(symbol);
    }

    private String getLtsSymbol(String symbol) {
        return subToLts.get(symbol);
    }
}
