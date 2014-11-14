package com.cyanspring.common.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Execution;

public class OpenPosition extends Position implements Cloneable {
	private static final Logger log = LoggerFactory
			.getLogger(OpenPosition.class);
	private double price;
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	protected OpenPosition() {
		super();
	}
	
	// this one is used by overall position
	public OpenPosition(String user, String account, String symbol, double qty, double price) {
		super(account + "-" + symbol, user, account, symbol, qty);
		this.price = price;
	}
	
	// this one is used by detail position
	public OpenPosition(Execution execution) {
		super(execution.getId(), execution.getUser(), execution.getAccount(), execution.getSymbol(),
				execution.getSide().isBuy()?execution.getQuantity():-execution.getQuantity());
		this.price = execution.getPrice();
	}

	public OpenPosition split(double qty) { // qty + for buy, - for sell
		OpenPosition pos = this.clone();
		pos.setQty(qty);
		this.setQty(this.getQty()-qty);
		return pos;
	}
	
	 
	@Override
	protected String formatString() {
		 return super.formatString() + ", " + this.price;
	}

	@Override
	public OpenPosition clone() {
		try {
			return (OpenPosition)super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
}
