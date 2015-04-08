package com.cyanspring.common.event.marketdata;

import java.util.Map;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;

public class QuoteExtEvent extends RemoteAsyncEvent {
	
	private DataObject data;
	private int sourceId;
	
	public QuoteExtEvent(String key, String receiver, DataObject data, int sourceId) {
		super(key, receiver);
		this.data = data;
		this.sourceId = sourceId;
	}
	
	public String getSymbol(){
		return data.get(String.class, QuoteExtDataField.SYMBOL.value());
	}
	
	public Map<String, Object> getFields() {
		return data.getFields();
	}
	
	public DataObject getQuoteExt(){
		return data;
	}
	
	public int getSourceId(){
		return sourceId;
	}

}
