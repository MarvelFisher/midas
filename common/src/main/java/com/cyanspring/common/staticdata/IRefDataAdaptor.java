package com.cyanspring.common.staticdata;

public interface IRefDataAdaptor {
    public boolean getStatus();
    public void setStatus(boolean status);
    public void flush();
    public void init() throws Exception;
    public void uninit();
    public void subscribeRefData(IRefDataListener listener) throws Exception;
    public void unsubscribeRefData(IRefDataListener listener);
}
