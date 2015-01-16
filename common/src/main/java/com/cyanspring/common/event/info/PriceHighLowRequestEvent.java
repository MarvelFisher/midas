package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PriceHighLowRequestEvent extends RemoteAsyncEvent {
	private PriceHighLowType type;
	private List<String>  symbolList ;

	public PriceHighLowRequestEvent(String key, String receiver, PriceHighLowType type)
	{
		super(key, receiver);
	}
	public PriceHighLowRequestEvent(String key, String receiver,
			PriceHighLowType type,
			List<String> symbolList) {
		super(key, receiver);
		this.type = type;
		this.symbolList = symbolList ;
	}

	public PriceHighLowType getType() {
		return type;
	}

	public List<String> getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(List<String> symbolList) {
		this.symbolList = symbolList;
	}

}