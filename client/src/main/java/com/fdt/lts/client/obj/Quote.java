package com.fdt.lts.client.obj;

import java.util.Date;

public class Quote {
	public String symbol;
	public double bid;
	public double ask;
	public double last;
	public double high;
	public double low;
	public double open;
	public double close;
	public Date timeStamp;
	public Date timeSent;
	public boolean stale;
	public String toString(){
		return symbol + ", bid: " + bid + ", ask: " + ask
				+ ", last: " + last + ", high: " + high
				+ ", low: " + low + ", open: " + open 
				+ ", close:" + close + ", Stamp:" + timeStamp
				+ ", sent: " + timeSent;
	}
}
