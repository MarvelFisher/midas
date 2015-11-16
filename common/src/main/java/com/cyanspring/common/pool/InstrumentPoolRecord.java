package com.cyanspring.common.pool;

import java.io.Serializable;

public class InstrumentPoolRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private String instrumentPoolId;
	private String symbol;
	private double qty;
	private double buyHeld;
	private double sellHeld;

	public String getInstrumentPoolId() {
		return instrumentPoolId;
	}

	public void setInstrumentPoolId(String instrumentPoolId) {
		this.instrumentPoolId = instrumentPoolId;
	}

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

	public double getBuyHeld() {
		return buyHeld;
	}

	public void setBuyHeld(double buyHeld) {
		this.buyHeld = buyHeld;
	}

	public double getSellHeld() {
		return sellHeld;
	}

	public void setSellHeld(double sellHeld) {
		this.sellHeld = sellHeld;
	}

	public double getUseableQty() {
		if (buyHeld > sellHeld) {
			return qty - buyHeld;
		} else {
			return qty - sellHeld;
		}
	}

	public void addBuyHeld(double held) {
		buyHeld += held;
	}

	public void addSellHeld(double held) {
		sellHeld += held;
	}
}
