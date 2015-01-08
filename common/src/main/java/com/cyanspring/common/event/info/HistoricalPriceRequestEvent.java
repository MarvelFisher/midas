package com.cyanspring.common.event.info;

import java.util.Date;

import com.cyanspring.common.event.RemoteAsyncEvent;

@SuppressWarnings("serial")
public class HistoricalPriceRequestEvent extends RemoteAsyncEvent 
{
	private String symbol;
	private String historyType ;
	private Date   startDate ;
	private Date   endDate ;

	public HistoricalPriceRequestEvent(String key, 
								  	   String receiver,
								  	   String symbol, 
								  	   String historyType, 
								  	   Date startDate, 
								  	   Date endDate) 
	{
		super(key, receiver);
		this.symbol = symbol ;
		this.historyType = historyType ;
		this.startDate = startDate ;
		this.endDate = endDate ;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getHistoryType() {
		return historyType;
	}

	public void setHistoryType(String historyType) {
		this.historyType = historyType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}