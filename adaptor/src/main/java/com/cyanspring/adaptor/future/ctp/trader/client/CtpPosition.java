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
	
	private double toQty;
	private double yeQty;
	private boolean isBuy;
	
	private boolean isToday;
	
	private final static double threshold = 10;
	
	public CtpPosition() {
		
	}
	
	public CtpPosition(String user, String symbol, double toQty, double yeQty) {
		this.user = user;
		this.symbol = symbol;
		this.toQty = toQty;
		this.yeQty = yeQty;
	}
	
//	public boolean buyZeroPosition() {
//		return PriceUtils.LessThan(buyQty, threshold);
//	}
//	
//	public boolean sellZeroPosition() {
//		return PriceUtils.LessThan(sellQty, threshold);
//	}
//	
//	protected void openBuyPosition(double qty){
//		buyQty += qty;
//	}
//	
//	protected void closeBuyPosition(double qty) {
//		buyQty -= qty;
//	}
//	
//	protected void openSellPosition(double qty) {
//		sellQty += qty;
//	}
//	
//	protected void closeSellPosition(double qty) {
//		sellQty -= qty;
//	}
	
	
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

	public double getToQty() {
		return toQty;
	}

	public void setToQty(double toQty) {
		this.toQty = toQty;
	}

	public double getYeQty() {
		return yeQty;
	}

	public void setYeQty(double yeQty) {
		this.yeQty = yeQty;
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
