package com.cyanspring.common.account;

import java.io.Serializable;

public class PositionPeakPrice implements Serializable{
	private String account;
	private String symbol;
	private double position;
	private double price;

	public PositionPeakPrice() {
		
	}
	
	public PositionPeakPrice(String account, String symbol, double position,
			double price) {
		super();
		this.account = account;
		this.symbol = symbol;
		this.position = position;
		this.price = price;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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
