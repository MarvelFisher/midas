package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.fx.IFxConverter;
public class RateConverterReplyEvent extends RemoteAsyncEvent{
	
	private IFxConverter converter;
	public RateConverterReplyEvent(String key, String receiver,IFxConverter converter) {
		super(key, receiver);
		this.converter = converter;
	}
	
	public IFxConverter getConverter() {
		return converter;
	}
}
