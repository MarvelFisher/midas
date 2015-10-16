package com.cyanspring.marketdata.adaptor;

import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.IMarketDataStateListener;
import com.cyanspring.common.marketdata.MarketDataException;

import java.util.List;

/**
 * Created by Shuwei on 2015/10/15.
 */
public class WindAdaptor implements IMarketDataAdaptor{
    @Override
    public void init() throws Exception {

    }

    @Override
    public void uninit() {

    }

    @Override
    public boolean getState() {
        return false;
    }

    @Override
    public void subscribeMarketDataState(IMarketDataStateListener listener) {

    }

    @Override
    public void unsubscribeMarketDataState(IMarketDataStateListener listener) {

    }

    @Override
    public void subscribeMarketData(String instrument, IMarketDataListener listener) throws MarketDataException {

    }

    @Override
    public void unsubscribeMarketData(String instrument, IMarketDataListener listener) {

    }

    @Override
    public void subscribeMultiMarketData(List<String> subscribeList, IMarketDataListener listener) throws MarketDataException {

    }

    @Override
    public void unsubscribeMultiMarketData(List<String> unSubscribeList, IMarketDataListener listener) {

    }

    @Override
    public void clean() {

    }
}
