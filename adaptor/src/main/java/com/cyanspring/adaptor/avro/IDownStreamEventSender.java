package com.cyanspring.adaptor.avro;


import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.common.transport.IObjectListener;

public interface IDownStreamEventSender extends IObjectListener{
	public void init() throws Exception;
	public void uninit() throws Exception;
	public void sendRemoteEvent(Object t, WrapObjectType type);
	public void setEventListener(IObjectListener listener);
}
