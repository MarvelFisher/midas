package com.cyanspring.cstw.gui.bean;

public class PositionStatisticBean{
	private String symbol;
	private double qty;
	private double PnL;
	private double AcPnL;
	private double Margin;
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getQty() {
		return qty;
	}
	public void setQty(double qty) {
		this.qty = qty;
	}
	public double getPnL() {
		return PnL;
	}
	public void setPnL(double pnL) {
		PnL = pnL;
	}
	public double getAcPnL() {
		return AcPnL;
	}
	public void setAcPnL(double acPnL) {
		AcPnL = acPnL;
	}
	public double getMargin() {
		return Margin;
	}
	public void setMargin(double margin) {
		Margin = margin;
	}

	
}
