package com.cyanspring.adaptor.future.wind;

import java.util.ArrayList;
import java.util.List;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.InnerQuote;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.id.Library.Util.FinalizeHelper;

public class UserClient implements AutoCloseable {

    List<String> list = new ArrayList<String>();

    public List<String> getList() {
        return list;
    }

    public IMarketDataListener listener = null;

    /**
     * @param mdListener
     */
    public UserClient(IMarketDataListener mdListener) {
        listener = mdListener;
    }

    /**
     * add symbol to list
     *
     * @param symbol e.g. USDJPY
     */
    public void addSymbol(String symbol) {
        synchronized (list) {
            if (!list.contains(symbol)) {
                list.add(symbol);
            }
        }
    }

    /**
     * remove symbol from list
     *
     * @param symbol
     */
    public void removeSymbol(String symbol) {
        synchronized (list) {
            if (list.contains(symbol)) {
                list.remove(symbol);
            }
        }
    }

    /**
     * check if list contains symbol
     *
     * @param symbol
     * @return
     */
    public boolean isMySymbol(String symbol) {
        synchronized (list) {
            return list.contains(symbol);
        }
    }

    public void sendTrade(Trade trade){
        if(isMySymbol(trade.getSymbol())){
            listener.onTrade(trade);
        }
    }

    public void sendInnerQuote(InnerQuote innerQuote){
        if(isMySymbol(innerQuote.getSymbol())){
            listener.onQuote(innerQuote);
        }
    }

    public void sendQuoteExtend(DataObject quoteExtend){
        if(isMySymbol(quoteExtend.get(String.class, QuoteExtDataField.SYMBOL.value()))){
            listener.onQuoteExt(quoteExtend, 101);
        }
    }

    void uninit() {

        if (list != null) {
            list.clear();
            list = null;
        }
        listener = null;
    }

    @Override
    public void close() throws Exception {
        uninit();
        FinalizeHelper.suppressFinalize(this);
    }

}
