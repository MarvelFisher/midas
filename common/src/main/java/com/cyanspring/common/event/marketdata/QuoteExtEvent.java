package com.cyanspring.common.event.marketdata;

import java.util.Map;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.QuoteExtDataField;
import com.cyanspring.common.marketdata.QuoteSource;

public class QuoteExtEvent extends RemoteAsyncEvent {
	
	private DataObject data;
	private QuoteSource quoteSource;
	
	public QuoteExtEvent(String key, String receiver, DataObject data, QuoteSource quoteSource) {
		super(key, receiver);
		this.data = data;
		this.quoteSource = quoteSource;
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

	public QuoteSource getQuoteSource() {
		return quoteSource;
	}
}
