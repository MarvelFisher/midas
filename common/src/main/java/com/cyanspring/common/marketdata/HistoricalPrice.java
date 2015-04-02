package com.cyanspring.common.marketdata;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.cyanspring.common.util.PriceUtils;

public class HistoricalPrice  implements Serializable, Comparable<HistoricalPrice>, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1903856150760396053L;
	private String tradedate = null ;
	private Date   keytime = null ;
	private Date   datatime = null ;
	String symbol = null ;
	double open ;
	double high ;
	double low ;
	double close ;
	long   volume ;
	
	public HistoricalPrice()
	{
	}
	public HistoricalPrice(String symbol, String tradedate, Date keytime)
	{
		this.symbol = symbol ;
		this.setTradedate(tradedate);
		this.setKeytime(keytime);
	}
	public HistoricalPrice( String tradedate,
					Date keytime, 
					Date datatime,
					String symbol, 
					double open, 
					double high, 
					double low, 
					double close, 
					int    volume)
	{

		this.setTradedate(tradedate);
		this.setKeytime(keytime);
		this.setDatatime(datatime);
		this.symbol = symbol ;
		this.open = open ;
		this.high = high ;
		this.low = low ;
		this.close = close ;
		this.volume = volume ;
	}
	public boolean setPrice(double price)
	{
		if (PriceUtils.isZero(price))
		{
			return false;
		}
		boolean changed = false; 
		if (PriceUtils.isZero(open))
		{
			open = price ;
			changed = true;
		}
		if (high < price)
		{
			high = price ;
			changed = true;
		}
		if (PriceUtils.isZero(low) || low > price)
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
		this.setTradedate(price.tradedate);
		this.setKeytime(price.keytime);
		this.setDatatime(price.datatime);
		this.setSymbol(price.getSymbol());
		this.setOpen(price.getOpen());
		this.setClose(price.getClose());
		this.setHigh(price.getHigh());
		this.setLow(price.getLow());
		this.setVolume(price.getVolume());
	}
	public void update(HistoricalPrice price)
	{
		if (PriceUtils.isZero(this.open))
		{
			this.open = price.open ;
		}
		if (!PriceUtils.isZero(price.close))
		{
			this.close = price.close;
		}
		if (this.high < price.high)
		{
			this.high = price.high;
		}
		if (PriceUtils.isZero(this.low) || this.low > price.low)
		{
			this.low = price.low;
		}
		this.volume += price.volume;
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
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}

	@Override
	public int compareTo(HistoricalPrice o) {
		return getKeytime().compareTo(o.getKeytime());
	}
	@Override
	public Object clone()
	{
		HistoricalPrice price = new HistoricalPrice();
		price.setTradedate(this.tradedate);
		price.setKeytime(this.keytime);
		price.setDatatime(this.datatime);
		price.setSymbol(this.symbol);
		price.setOpen(this.open);
		price.setClose(this.close);
		price.setHigh(this.high);
		price.setLow(this.low);
		price.setVolume(this.volume);
		return price ;
	}
	public String getTradedate() {
		return tradedate;
	}
	public void setTradedate(String tradedate) {
		this.tradedate = tradedate;
	}
	public Date getKeytime() {
		return keytime;
	}
	public void setKeytime(Date keytime) {
		this.keytime = keytime;
	}
	public Date getDatatime() {
		return datatime;
	}
	public void setDatatime(Date datatime) {
		this.datatime = datatime;
	}

}
