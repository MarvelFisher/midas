package com.cyanspring.common.event.info;

import java.util.List;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.HistoricalPrice;

public class HistoricalPriceEvent extends RemoteAsyncEvent  {
	List<HistoricalPrice> priceList ;
	String historyType ;
	String symbol ;
	int dataCount ;
	private boolean isSuccess ;
	private String errorMsg ;

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

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
