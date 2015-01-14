package com.cyanspring.common.marketdata;

import java.io.Serializable;
import java.util.Date;

public class HistoricalPrice  implements Serializable {
	Date   timestamp ;
	String symbol ;
	double open ;
	double high ;
	double low ;
	double close ;
	int    volumn ;
	
	HistoricalPrice(Date   timestamp, 
					String symbol, 
					double open, 
					double high, 
					double low, 
					double close, 
					int    volumn)
	{
		this.timestamp = timestamp ;
		this.symbol = symbol ;
		this.open = open ;
		this.high = high ;
		this.low = low ;
		this.close = close ;
		this.volumn = volumn ;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public int getVolumn() {
		return volumn;
	}
	public void setVolumn(int volumn) {
		this.volumn = volumn;
	}

}
