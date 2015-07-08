package com.cyanspring.adaptor.future.ctp.trader.client;

import java.io.Serializable;

import com.cyanspring.common.util.PriceUtils;

/**
 * encapsulate complex of future position
 * @author Marvel
 */
public class CtpPosition implements Serializable{
	private String symbol;
	private double tdQty;
	private double ydQty;
	private boolean isBuy;
	
	public CtpPosition(String symbol, boolean isBuy, double tdQty, double ydQty) {
		this.symbol = symbol;
		this.tdQty = tdQty;
		this.ydQty = ydQty;
		this.isBuy = isBuy;
	}
	
	public void add(CtpPosition position) {
		this.tdQty += position.getTdQty();
		this.ydQty += position.getYdQty();
	}
	
	////getters and setters ////
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
	
	@Override
	public String toString() {
		return symbol + "," + isBuy + "," + ydQty + "," + tdQty;
	}
}
