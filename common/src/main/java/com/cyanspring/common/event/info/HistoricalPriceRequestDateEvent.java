package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class HistoricalPriceRequestDateEvent extends RemoteAsyncEvent 
{
	private String symbol;
	private String historyType ;
	private String startDate;
	private String endDate;

	public HistoricalPriceRequestDateEvent(String key, String receiver)
	{
		super(key, receiver);
	}
	public HistoricalPriceRequestDateEvent(	String key, 
											String receiver,
											String symbol, 
											String historyType, 
											String startDate,
											String endDate) 
	{
		super(key, receiver);
		this.setSymbol(symbol) ;
		setHistoryType(historyType);
		setStartDate(startDate);
		setEndDate(endDate);
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
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

}
