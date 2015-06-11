package com.cyanspring.server.validation.data;

import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.data.DataObject;

public interface IQuoteExtProvider {
	
	public ConcurrentHashMap<String, DataObject> getQuoteExtMap();
}
