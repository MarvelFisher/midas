package com.cyanspring.common.alert;

import com.cyanspring.common.util.IdGenerator;

public class PriceAlert {
	private String id;
	private String user;
	private String account;
	private String symbol;
	private double price;
	private double startPrice;
	
	public PriceAlert(String user, String account, String symbol, double price) {
		super();
		this.id = "A" + IdGenerator.getInstance().getNextID();
		this.user = user;
		this.account = account;
		this.symbol = symbol;
		this.price = price;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public String getId() {
		return id;
	}

	public String getUser() {
		return user;
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
	
	@Override
	public String toString() {
		return "[" + user + ", " + account + ", " + symbol + ", " + price + ", " + startPrice + "]";
	}
}
