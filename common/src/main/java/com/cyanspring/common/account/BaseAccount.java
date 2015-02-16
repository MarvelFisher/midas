package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.Date;

import com.cyanspring.common.Clock;
import com.cyanspring.common.util.PriceUtils;

public abstract class BaseAccount implements Serializable {
	private String id;
	private String userId;
	private String market;
	private double PnL;
	private double urPnL;
	private double allTimePnL;
	private boolean active;
	private String currency;
	private double cash;
	private double cashDeposited;
	private double rollPrice = 1.0;
	private double margin;
	//private double commission;
	//private boolean cashAvailable;	

	private Date created;
	
	protected BaseAccount() {
		this.created = Clock.getInstance().now();
	}
	
	public BaseAccount(String id, String userId) {
		this();
		this.id = id;
		this.userId = userId;
	}
	
	public synchronized double getPnL() {
		return PnL;
	}

	protected synchronized void setPnL(double pnL) {
		PnL = pnL;
	}

	public synchronized double getUrPnL() {
		return urPnL;
	}

	public synchronized void setUrPnL(double urPnL) {
		this.urPnL = urPnL;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

	public synchronized String getId() {
		return id;
	}

	public synchronized String getUserId() {
		return userId;
	}

	public synchronized String getCurrency() {
		return currency;
	}

	public synchronized double getCash() {
		return cash;
	}

	public synchronized double getValue() {
		return cash + urPnL;
	}

	public synchronized double getDailyPnL() {
		return this.PnL + this.urPnL;
	}

	public synchronized void setId(String id) {
		this.id = id;
	}

	public synchronized void setUserId(String userId) {
		this.userId = userId;
	}

	public synchronized void setCurrency(String currency) {
		this.currency = currency;
	}

	public synchronized Date getCreated() {
		return created;
	}

	protected synchronized void setCreated(Date created) {
		this.created = created;
	}

	protected synchronized void setCash(double cash) {
		this.cash = cash;
	}

	public synchronized double getMargin() {
		return margin;
	}

	public synchronized  void setMargin(double margin) {
		this.margin = margin;
	}
	
	public synchronized String getMarket() {
		return market;
	}

	public synchronized void setMarket(String market) {
		this.market = market;
	}

	public synchronized double getAllTimePnL() {
		return allTimePnL;
	}

	protected synchronized void setAllTimePnL(double allTimePnL) {
		this.allTimePnL = allTimePnL;
	}
	
	public synchronized double getCashDeposited() {
		return cashDeposited;
	}

	protected synchronized void setCashDeposited(double cashDeposited) {
		this.cashDeposited = cashDeposited;
	}

	public synchronized double getUnitPrice() {
		if(!PriceUtils.isZero(this.cashDeposited)) {
			return rollPrice + this.urPnL/this.cashDeposited;
		}
		return rollPrice;
	}

	protected void setUnitPrice(double unitPrice) {
	}
	
	public synchronized double getRollPrice() {
		return rollPrice;
	}

	protected synchronized void setRollPrice(double rollPrice) {
		this.rollPrice = rollPrice;
	}

	//public synchronized double getCommission() {
	//	return commission;
	//}

	//public synchronized void setCommission(double commission) {
	//	this.commission = commission;
	//}
	
	//public synchronized boolean isCashAvailable() {
	//	return cashAvailable;
	//}

	//public synchronized void setCashAvailable(boolean cashAvailable) {
	//	this.cashAvailable = cashAvailable;
	//}
	
	// end of getters/setters
	
	public synchronized void addMargin(double value) {
		this.margin += value;
	}

	public synchronized void addPnL(double value) {
		this.PnL += value;
	}
	
	public synchronized void addAllTimePnL(double value) {
		this.allTimePnL += value;
	}

	public synchronized void addCash(double value) {
		this.cash += value;
		this.cashDeposited += value;
	}
	
	public synchronized void updatePnL(double pnl) {
		addPnL(pnl);
		addAllTimePnL(pnl);
		this.cash += pnl;
	}
	
	public synchronized void updateEndOfDay() {
		if(!PriceUtils.isZero(this.cashDeposited))
			this.rollPrice += this.PnL/this.cashDeposited;
	}

	public synchronized void resetDailyPnL() {
		this.PnL = 0.0;
	}

	@Override
	public synchronized String toString() {
		return "[" + this.id + ", " + this.userId + ", " + this.cash + "]";
	}
	
}
