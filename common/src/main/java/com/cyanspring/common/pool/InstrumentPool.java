package com.cyanspring.common.pool;

public class InstrumentPool {
	String id;
	String exchangeSubAccount;
	String symbol;
	double qty;
	double held;
	public InstrumentPool(String id, String exchangeSubAccount, String symbol,
			double qty) {
		super();
		this.id = id;
		this.exchangeSubAccount = exchangeSubAccount;
		this.symbol = symbol;
		this.qty = qty;
	}
	
	public String getId() {
		return id;
	}
	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}
	public String getSymbol() {
		return symbol;
	}
	public double getQty() {
		return qty;
	}
	public double getHeld() {
		return held;
	}
	
	public void addHeld(double qty) {
		held += qty;
	}
}
