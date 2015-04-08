package com.cyanspring.common.account;

public class PositionPeakPrice {
	private String account;
	private String symbol;
	private double position;
	private double price;

	public PositionPeakPrice(String account, String symbol, double position,
			double price) {
		super();
		this.account = account;
		this.symbol = symbol;
		this.position = position;
		this.price = price;
	}
	
	public double getPosition() {
		return position;
	}
	public String getAccount() {
		return account;
	}
	public String getSymbol() {
		return symbol;
	}
	public double getPrice() {
		return price;
	}
	public void setPosition(double position) {
		this.position = position;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public PositionPeakPrice clone() {
		return (PositionPeakPrice)clone();
	}
}
