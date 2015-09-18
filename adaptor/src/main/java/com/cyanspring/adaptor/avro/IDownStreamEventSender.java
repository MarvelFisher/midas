package com.cyanspring.adaptor.avro;


import com.cyanspring.common.transport.IObjectListener;

public interface IDownStreamEventSender extends IObjectListener{
	public void init() throws Exception;
	public void uninit() throws Exception;
	public void sendRemoteEvent(Object t);
	public void setEventListener(IObjectListener listener);
}
