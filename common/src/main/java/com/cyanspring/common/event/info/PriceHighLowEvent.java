package com.cyanspring.common.event.info;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class PriceHighLowEvent extends RemoteAsyncEvent {
	
	private PriceHighLowType type;
	private String symbol ;
	private double high;
	private double low;

	public PriceHighLowEvent(String key, String receiver) {
		super(key, receiver);
	}
	public PriceHighLowEvent(String key, String receiver,
			PriceHighLowType type, String symbol, double high, double low) {
		super(key, receiver);
		this.setType(type);
		this.setSymbol(symbol) ;
		this.setHigh(high);
		this.setLow(low);
	}
	public PriceHighLowType getType() {
		return type;
	}
	public double getHigh() {
		return high;
	}
	public double getLow() {
		return low;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public void setType(PriceHighLowType type) {
		this.type = type;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public void setLow(double low) {
		this.low = low;
	}
}
