package com.cyanspring.common.event.marketdata;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.SymbolInfo;

public class SymbolEvent extends RemoteAsyncEvent {

	public SymbolEvent(String key, String receiver, List<SymbolInfo> symbolInfoList) {
		super(key, receiver);
		this.setSymbolInfoList(symbolInfoList) ;
	}

	private List<SymbolInfo> symbolInfoList;
	
	public List<SymbolInfo> getSymbolInfoList() {
		return symbolInfoList;
	}

	public void setSymbolInfoList(List<SymbolInfo> symbolInfoList) {
		this.symbolInfoList = symbolInfoList;
	}
	
	
}
