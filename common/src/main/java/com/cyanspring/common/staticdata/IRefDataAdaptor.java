package com.cyanspring.common.staticdata;

public interface IRefDataAdaptor {
    public void subscribeRefData(IRefDataListener listener);
    public void unsubscribeRefData(IRefDataListener listener);
}
