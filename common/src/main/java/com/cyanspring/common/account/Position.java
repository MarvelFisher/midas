package com.cyanspring.common.account;

import java.util.Date;

import com.cyanspring.common.Clock;
import com.cyanspring.common.util.PriceUtils;

public abstract class Position {
	private String id;
	private String account;
	private String user;
	private String symbol;
	private double qty;
	private double PnL;
	private Date created;
	private double acPnL;

	protected Position() {
		
	}
	
	protected Position(String id, String user, String account, String symbol, double qty) {
		this.user = user;
		this.account = account;
		this.symbol = symbol;
		this.id = id;
		this.qty = qty;
		this.created = Clock.getInstance().now();
	}
	
	//// getters and setters ////
	public double getPnL() {
		return PnL;
	}
	public void setPnL(double pnL) {
		PnL = pnL;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getQty() {
		return qty;
	}
	
	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	protected void setCreated(Date date) {
		this.created = date;
	}

	protected void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}
	
	public double getAcPnL() {
		return acPnL;
	}

	public void setAcPnL(double acPnL) {
		this.acPnL = acPnL;
	}
	
	///// end of getters and setters ////////

	public String getAccount() {
		return account;
	}

	protected void setAccount(String account) {
		this.account = account;
	}

	public String getUser() {
		return user;
	}

	protected void setUser(String user) {
		this.user = user;
	}

	public double addQty(double qty) {
		return this.qty += qty;
	}

	public boolean zeroPosition(double position) {
		return PriceUtils.isZero(this.qty + position);
	}
	
	public boolean zeroPosition(OpenPosition position) {
		return PriceUtils.isZero(this.qty + position.getQty());
	}
	
	public boolean oppositePosition(double position) {
		return PriceUtils.GreaterThan(this.qty, 0) && PriceUtils.LessThan(position, 0) ||
				PriceUtils.LessThan(this.qty, 0) && PriceUtils.GreaterThan(position, 0);
	}

	public boolean oppositePosition(OpenPosition position) {
		return oppositePosition(position.getQty());
	}
	
	public boolean isBuy() {
		return PriceUtils.GreaterThan(this.qty, 0);
	}
	
	protected String formatString() {
		return this.id + ", " + this.account + ", " + this.symbol  + ", " + 
				this.qty + ", " + this.PnL;
	}
	
	@Override
	public synchronized String toString() {
		return "[" + formatString() + "]";
	}
}
