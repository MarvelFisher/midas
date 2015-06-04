package com.cyanspring.common.event.info;

import java.util.Date;
import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class HistoricalPriceRequestEvent extends RemoteAsyncEvent 
{
	private String symbol;
	private String historyType ;
	private int dataCount;

	public HistoricalPriceRequestEvent(String key, String receiver)
	{
		super(key, receiver);
	}
	public HistoricalPriceRequestEvent(String key, 
								  	   String receiver,
								  	   String symbol, 
								  	   String historyType, 
								  	   int dataCount) 
	{
		super(key, receiver);
		this.setSymbol(symbol) ;
		this.historyType = historyType ;
		this.setDataCount(dataCount);
	}

	public String getHistoryType() {
		return historyType;
	}
	public void setHistoryType(String historyType) {
		this.historyType = historyType;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

}