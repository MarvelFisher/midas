package com.cyanspring.common.staticdata;

public interface IRefDataAdaptor {
    public void init();
    public void uninit();
    public void subscribeRefData(IRefDataListener listener);
    public void unsubscribeRefData(IRefDataListener listener);
}
