package com.cyanspring.common.marketdata;

import com.cyanspring.common.event.info.PriceHighLowType;

public class PriceHighLow {
	private PriceHighLowType type;
	private String symbol ;
	private double high;
	private double low;
	public PriceHighLow()
	{}
	public PriceHighLow(String symbol, double high, double low, PriceHighLowType type)
	{
		this.symbol = symbol ;
		this.high = high ;
		this.low = low ;
		this.type = type ;
	}
	public PriceHighLowType getType() {
		return type;
	}
	public void setType(PriceHighLowType type) {
		this.type = type;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

}
