package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.HistoricalPrice;

public class HistoricalPriceEvent extends RemoteAsyncEvent  {
	List<HistoricalPrice> priceList ;
	String historyType ;
	String symbol ;
	int dataCount ;
	private boolean ok ;
	private String message ;
	private long currentTotalVolume;
	private long currentTurnover;

	public HistoricalPriceEvent(String key, String receiver)
	{
		super(key, receiver);
	}
	public HistoricalPriceEvent(String key, String receiver, String symbol, String historyType, int dataCount) {
		super(key, receiver);
		this.symbol = symbol ;
		this.historyType = historyType ;
		this.dataCount = dataCount ;
		// TODO Auto-generated constructor stub
	}

	public List<HistoricalPrice> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<HistoricalPrice> priceList) {
		this.priceList = priceList;
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
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	public long getCurrentTotalVolume() {
		return currentTotalVolume;
	}
	public void setCurrentTotalVolume(long currentTotalVolume) {
		this.currentTotalVolume = currentTotalVolume;
	}
	public long getCurrentTurnover() {
		return currentTurnover;
	}
	public void setCurrentTurnover(long currentTurnover) {
		this.currentTurnover = currentTurnover;
	}

}
