package com.cyanspring.adaptor.future.ctp.trader.client;

import java.io.Serializable;

import com.cyanspring.common.util.PriceUtils;

/**
 * encapsulate complex of future position
 * @author Marvel
 */
public class CtpPosition implements Serializable{
	private String user;
	private String symbol;
	
//	private double buyQty;
//	private double sellQty;
	
	private double tdQty;
	private double ydQty;
	private boolean isBuy;
	
	private boolean isToday;
	
	private final static double threshold = 10;
	
	public CtpPosition() {
		
	}
	
	public CtpPosition(String user, String symbol, double tdQty, double ydQty) {
		this.user = user;
		this.symbol = symbol;
		this.tdQty = tdQty;
		this.ydQty = ydQty;
	}
	
	
	////getters and setters ////
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getTdQty() {
		return tdQty;
	}

	public void setTdQty(double toQty) {
		this.tdQty = toQty;
	}

	public double getYdQty() {
		return ydQty;
	}

	public void setYdQty(double yeQty) {
		this.ydQty = yeQty;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}

	public boolean isToday() {
		return isToday;
	}

	public void setToday(boolean isToday) {
		this.isToday = isToday;
	}
	
}
