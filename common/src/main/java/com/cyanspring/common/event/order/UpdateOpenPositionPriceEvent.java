package com.cyanspring.common.event.order;

import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class UpdateOpenPositionPriceEvent extends RemoteAsyncEvent {
	
	private double price;
	private String symbol;
	private String account;
	public UpdateOpenPositionPriceEvent(String key, String receiver, double price, String symbol, String account) {
		super(key, receiver);
		this.price = price;
		this.symbol = symbol;
		this.account = account;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}
