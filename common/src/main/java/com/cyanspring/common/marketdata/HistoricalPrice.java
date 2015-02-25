package com.cyanspring.common.marketdata;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HistoricalPrice  implements Serializable, Comparable<HistoricalPrice>, Cloneable {
	Date   timestamp = null ;
	String symbol = null ;
	double open ;
	double high ;
	double low ;
	double close ;
	int    volume ;
	
	public HistoricalPrice()
	{
	}
	public HistoricalPrice(String symbol, boolean defaultTime)
	{
		this.symbol = symbol ;
		if (defaultTime)
		{
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.DATE, -1);
			this.timestamp = cal.getTime();
		}
	}
	public HistoricalPrice(Date   timestamp, 
					String symbol, 
					double open, 
					double high, 
					double low, 
					double close, 
					int    volume)
	{
		this.timestamp = timestamp ;
		this.symbol = symbol ;
		this.open = open ;
		this.high = high ;
		this.low = low ;
		this.close = close ;
		this.volume = volume ;
	}
	public boolean setPrice(double price)
	{
		boolean changed = false; 
		if (open == 0)
		{
			open = price ;
			changed = true;
		}
		if (high < price)
		{
			high = price ;
			changed = true;
		}
		if (low == 0 || low > price)
		{
			low = price ;
			changed = true;
		}
		if (close != price) changed = true;
		close = price ;
		return changed;
	}
	public void copy(HistoricalPrice price)
	{
		this.setTimestamp(price.getTimestamp());
		this.setSymbol(price.getSymbol());
		this.setOpen(price.getOpen());
		this.setClose(price.getClose());
		this.setHigh(price.getHigh());
		this.setLow(price.getLow());
		this.setVolume(price.getVolume());
	}
	public void update(HistoricalPrice price)
	{
		if (this.open == 0)
		{
			this.open = price.open ;
		}
		this.close = price.close;
		if (this.high < price.high)
		{
			this.high = price.high;
		}
		if (this.low > price.low)
		{
			this.low = price.low;
		}
		this.volume += price.volume;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		if (timestamp == null)
			return;
		this.timestamp = (Date)timestamp.clone();
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = String.format("%s", symbol) ;
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
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}

	@Override
	public int compareTo(HistoricalPrice o) {
		return timestamp.compareTo(o.getTimestamp());
	}
	@Override
	public Object clone()
	{
		HistoricalPrice price = new HistoricalPrice();
		price.setTimestamp(this.timestamp);
		price.setSymbol(this.symbol);
		price.setOpen(this.open);
		price.setClose(this.close);
		price.setHigh(this.high);
		price.setLow(this.low);
		price.setVolume(this.volume);
		return price ;
	}

}
