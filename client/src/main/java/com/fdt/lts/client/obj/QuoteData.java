package com.fdt.lts.client.obj;

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class QuoteData {
	//for thread safe 
	private final ReentrantLock lock = new ReentrantLock();

	private String symbol;
	private double bid;
	private double ask;
	private double last;
	private double high;
	private double low;
	private double open;
	private double close;
	private Date timeStamp;
	private Date timeSent;
	private boolean stale;
	
	public String getSymbol() {
			return symbol;
	}
	public void setSymbol(String symbol) {
		try{
			lock.lock();
			this.symbol = symbol;
		}finally{
			lock.unlock();
		}
	}
	public double getBid() {
			return bid;
	}
	public void setBid(double bid) {
		try{
			lock.lock();
			this.bid = bid;
		}finally{
			lock.unlock();
		}
	}
	public double getAsk() {
			return ask;
	}
	public void setAsk(double ask) {
		try{
			lock.lock();
			this.ask = ask;
		}finally{
			lock.unlock();
		}
	}
	public double getLast() {
			return last;
	}
	public void setLast(double last) {
		try{
			lock.lock();
			this.last = last;
		}finally{
			lock.unlock();
		}
	}
	public double getHigh() {
			return high;
	}
	public void setHigh(double high) {
		try{
			lock.lock();
			this.high = high;
		}finally{
			lock.unlock();
		}
	}
	public double getLow() {
			return low;
	}
	public void setLow(double low) {
		try{
			lock.lock();
			this.low = low;
		}finally{
			lock.unlock();
		}
	}
	public double getOpen() {
			return open;
	}
	public void setOpen(double open) {
		try{
			lock.lock();
			this.open = open;
		}finally{
			lock.unlock();
		}
	}
	public double getClose() {
			return close;
	}
	public void setClose(double close) {
		try{
			lock.lock();
			this.close = close;
		}finally{
			lock.unlock();
		}
	}
	public Date getTimeStamp() {
			return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		try{
			lock.lock();
			this.timeStamp = timeStamp;
		}finally{
			lock.unlock();
		}
	}
	public Date getTimeSent() {
			return timeSent;
	}
	public void setTimeSent(Date timeSent) {
		try{
			lock.lock();
			this.timeSent = timeSent;
		}finally{
			lock.unlock();
		}
	}
	public boolean isStale() {
			return stale;
	}
	public void setStale(boolean stale) {
		try{
			lock.lock();
			this.stale = stale;
		}finally{
			lock.unlock();
		}
		
		
	}
	
	
	public String toString(){
		return symbol + ", bid: " + bid + ", ask: " + ask
				+ ", last: " + last + ", high: " + high
				+ ", low: " + low + ", open: " + open 
				+ ", close:" + close + ", Stamp:" + timeStamp
				+ ", sent: " + timeSent;
	}
}
