package com.fdt.lts.client.obj;

import java.util.Date;
import java.util.List;

public class AccountInfo {
	private Account account;
	private List<ClosePosition> closePositions;
	private List<OpenPosition> openPositions;
	private List<Execution> executions;
	
	public class Position{
		private String id;
		private String account;
		private String user;
		private String symbol;
		private double qty;
		private double PnL;
		private Date created;
		private double acPnL;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getAccount() {
			return account;
		}
		public void setAccount(String account) {
			this.account = account;
		}
		public String getUser() {
			return user;
		}
		public void setUser(String user) {
			this.user = user;
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
		public double getPnL() {
			return PnL;
		}
		public void setPnL(double pnL) {
			PnL = pnL;
		}
		public Date getCreated() {
			return created;
		}
		public void setCreated(Date created) {
			this.created = created;
		}
		public double getAcPnL() {
			return acPnL;
		}
		public void setAcPnL(double acPnL) {
			this.acPnL = acPnL;
		}
	}
	
	public class ClosePosition extends Position{
		private double buyPrice;
		private double sellPrice; // close price is always sell price
		public double getBuyPrice() {
			return buyPrice;
		}
		public void setBuyPrice(double buyPrice) {
			this.buyPrice = buyPrice;
		}
		public double getSellPrice() {
			return sellPrice;
		}
		public void setSellPrice(double sellPrice) {
			this.sellPrice = sellPrice;
		}
	}
	
	public class OpenPosition extends Position{
		private double price;
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public double getMargin() {
			return margin;
		}
		public void setMargin(double margin) {
			this.margin = margin;
		}
		private double margin;
	}
	
	public class Account{
		private String id;
		private String userId;
		private String market;
		private double PnL;
		private double urPnL;
		private double allTimePnL;
		private String currency;
		private double cash;
		private double cashDeposited;
		private double rollPrice;
		private double margin;
		private double cashAvailable;
		private double marginHeld;
	}
	
	public class Execution{ // need to check the fields
		
	}

	public Account getAccount() {
		return account;
	}

	public List<ClosePosition> getClosePositions() {
		return closePositions;
	}

	public List<OpenPosition> getOpenPositions() {
		return openPositions;
	}

	public List<Execution> getExecutions() {
		return executions;
	}
}
