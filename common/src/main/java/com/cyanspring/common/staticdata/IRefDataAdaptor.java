package com.cyanspring.common.staticdata;

public interface IRefDataAdaptor {
    public void init() throws Exception;
    public void uninit();
    public void subscribeRefData(IRefDataListener listener) throws Exception;
    public void unsubscribeRefData(IRefDataListener listener);
}
