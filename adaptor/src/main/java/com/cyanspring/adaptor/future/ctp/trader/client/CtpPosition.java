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
	private double totalOpenPosition;
	
	public CtpPosition(String symbol, boolean isBuy, double tdQty, double ydQty, double totalOpenPosition) {
		this.symbol = symbol;
		this.tdQty = tdQty;
		this.ydQty = ydQty;
		this.isBuy = isBuy;
		this.totalOpenPosition = totalOpenPosition;
	}
	
	public void add(CtpPosition position) {
		this.tdQty += position.getTdQty();
		this.ydQty += position.getYdQty();
	}
	
	////getters and setters ////
	public String getSymbol() {
		return symbol;
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

	public double getTotalOpenPosition() {
		return totalOpenPosition;
	}

	@Override
	public String toString() {
		return symbol + "," + isBuy + "," + ydQty + "," + tdQty;
	}
}
