package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.util.IdGenerator;

public class OverallPosition implements Serializable, Cloneable {
	private static final long serialVersionUID = 276445262592182213L;
	private String id;
	private String account;
	private String user;
	private String exchangeSubAccount;
	private String symbol;
	private double qty;	  //current quantity
	private double buyPrice;
	private double sellPrice; // close price is always sell price
	private double buyQty;
	private double sellQty;
	private double UrPnL;
	private double PnL;
	private double commission;
	private double execCount;
	private Date lastUpdate;
	private ConcurrentHashMap<String, String> parentOrderMap = new ConcurrentHashMap<String, String>();

	public OverallPosition() {
	}

	public OverallPosition(String user, String account,
			String exchangeSubAccount, String symbol) {
		this.id = IdGenerator.getInstance().getNextID();
		this.user = user;
		this.account = account;
		this.exchangeSubAccount = exchangeSubAccount;
		this.symbol = symbol;
	}

	public void updateExecution(Execution execution) {
		if (execution.getSide().isBuy()) {
			this.buyPrice = (this.buyPrice * this.buyQty + execution.getPrice()
					* execution.getQuantity())
					/ (this.buyQty + execution.getQuantity());
			this.buyQty += execution.getQuantity();
		} else {
			this.sellPrice = (this.sellPrice * this.sellQty + execution
					.getPrice() * execution.getQuantity())
					/ (this.sellQty + execution.getQuantity());
			this.sellQty += execution.getQuantity();
		}
		// calculate execCount
		if (!parentOrderMap.containsKey(execution.getParentOrderId())) {
			this.execCount++;
			parentOrderMap.put(execution.getParentOrderId(),
					execution.getParentOrderId());
		}
		this.lastUpdate = Clock.getInstance().now();
	}

	public double getBuyPrice() {
		return buyPrice;
	}

	public double getSellPrice() {
		return sellPrice;
	}

	public double getTotalQty() {
		return buyQty + sellQty;
	}

	public double getRemainingQty() {
		return buyQty - sellQty;
	}

	public double getTurnover() {
		return buyPrice * buyQty + sellPrice * sellQty;
	}

	public double getExecCount() {
		return execCount;
	}

	void setExecCount(double execCount) {
		this.execCount = execCount;
	}

	public void removeTerminatedParentOrder(String parentOrderId) {
		parentOrderMap.remove(parentOrderId);
	}

	public double getCommission() {
		return commission;
	}

	public double getBuyQty() {
		return buyQty;
	}

	public double getSellQty() {
		return sellQty;
	}

	public double getOpenQty() {
		return buyQty - sellQty;
	}

	public String getId() {
		return id;
	}

	public String getAccount() {
		return account;
	}

	public String getUser() {
		return user;
	}

	public String getExchangeSubAccount() {
		return exchangeSubAccount;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getUrPnL() {
		return UrPnL;
	}

	public void setUrPnL(double urPnL) {
		UrPnL = urPnL;
	}

	public double getPnL() {
		return PnL;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void updateCommission(double value) {
		commission += value;
	}

	public void updatePnL(double value) {
		this.PnL += value;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	void setUser(String user) {
		this.user = user;
	}

	void setExchangeSubAccount(String exchangeSubAccount) {
		this.exchangeSubAccount = exchangeSubAccount;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setBuyPrice(double buyPrice) {
		this.buyPrice = buyPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public void setBuyQty(double buyQty) {
		this.buyQty = buyQty;
	}

	public void setSellQty(double sellQty) {
		this.sellQty = sellQty;
	}

	public void setPnL(double pnL) {
		PnL = pnL;
	}

	void setCommission(double commission) {
		this.commission = commission;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	public double getAvgPrice() {
		return ((buyPrice+sellPrice)/2);
	}

	@Override
	public synchronized OverallPosition clone()
			throws CloneNotSupportedException {
		return (OverallPosition) super.clone();
	}
}