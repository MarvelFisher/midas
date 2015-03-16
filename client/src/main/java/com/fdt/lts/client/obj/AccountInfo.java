package com.fdt.lts.client.obj;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountInfo {
	private Account account;
	// private List<ClosePosition> closePositions;
	private ConcurrentHashMap<String, List<OpenPosition>> openPositions;
//	private List<OpenPosition> openPositions;
	private ConcurrentHashMap<String, List<Execution>> executions;

	public Account newAccount() {
		account = new Account();
		return account;
	}

	public void addOpenPosition(String symbol, String orderID, OpenPosition oposition) {
		if (openPositions == null)
			openPositions = new ConcurrentHashMap<String, List<OpenPosition>>();
		List<OpenPosition> opList = openPositions.get(symbol);
		if(opList == null){
			opList = new ArrayList<OpenPosition>();
			openPositions.put(symbol, opList);
		}
		for(OpenPosition position : opList){
			if(position.getId().equals(orderID))
				opList.remove(orderID);
		}
		opList.add(oposition);
	}

	public void addExecution(String symbol, String orderID, Execution e) {
		if (executions == null)
			executions = new ConcurrentHashMap<String, List<Execution>>();
		List<Execution> exeList = executions.get(symbol);
		if(exeList == null){
			exeList = new ArrayList<Execution>();
			executions.put(symbol, exeList);
		}
		for(Execution exe : exeList){
			if(exe.getId().equals(orderID))
				exeList.remove(orderID);
		}
		exeList.add(e);
	}

	public class Position {
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

	// public class ClosePosition extends Position{
	// private double buyPrice;
	// private double sellPrice; // close price is always sell price
	// public double getBuyPrice() {
	// return buyPrice;
	// }
	// public void setBuyPrice(double buyPrice) {
	// this.buyPrice = buyPrice;
	// }
	// public double getSellPrice() {
	// return sellPrice;
	// }
	// public void setSellPrice(double sellPrice) {
	// this.sellPrice = sellPrice;
	// }
	// }

	public class OpenPosition extends Position {
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

	public class Account {
		private String id;
		private String userId;
		private String market;
		private double PnL;
		private double urPnL;
		private double allTimePnL;
		private String currency;
		private double cash;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getMarket() {
			return market;
		}

		public void setMarket(String market) {
			this.market = market;
		}

		public double getPnL() {
			return PnL;
		}

		public void setPnL(double pnL) {
			PnL = pnL;
		}

		public double getUrPnL() {
			return urPnL;
		}

		public void setUrPnL(double urPnL) {
			this.urPnL = urPnL;
		}

		public double getAllTimePnL() {
			return allTimePnL;
		}

		public void setAllTimePnL(double allTimePnL) {
			this.allTimePnL = allTimePnL;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public double getCash() {
			return cash;
		}

		public void setCash(double cash) {
			this.cash = cash;
		}
	}

	public class Execution { // need to check the fields
		private Date created;
		private Date modified;
		private String id;
		private String serverID;
		private String symbol;
		private String side;
		private long quantity;
		private double price;
		private String orderID;
		private String parentOrderID;
		private String strategyID;
		private String execID;
		private String user;
		private String account;
		public Date getCreated() {
			return created;
		}
		public void setCreated(Date created) {
			this.created = created;
		}
		public Date getModified() {
			return modified;
		}
		public void setModified(Date modified) {
			this.modified = modified;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getServerID() {
			return serverID;
		}
		public void setServerID(String serverID) {
			this.serverID = serverID;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public String getSide() {
			return side;
		}
		public void setSide(String side) {
			this.side = side;
		}
		public long getQuantity() {
			return quantity;
		}
		public void setQuantity(long quantity) {
			this.quantity = quantity;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public String getOrderID() {
			return orderID;
		}
		public void setOrderID(String orderID) {
			this.orderID = orderID;
		}
		public String getParentOrderID() {
			return parentOrderID;
		}
		public void setParentOrderID(String parentOrderID) {
			this.parentOrderID = parentOrderID;
		}
		public String getStrategyID() {
			return strategyID;
		}
		public void setStrategyID(String strategyID) {
			this.strategyID = strategyID;
		}
		public String getExecID() {
			return execID;
		}
		public void setExecID(String execID) {
			this.execID = execID;
		}
		public String getUser() {
			return user;
		}
		public void setUser(String user) {
			this.user = user;
		}
		public String getAccount() {
			return account;
		}
		public void setAccount(String account) {
			this.account = account;
		}		
	}

	public Account getAccount() {
		return account;
	}

	// public List<ClosePosition> getClosePositions() {
	// return closePositions;
	// }

	public List<OpenPosition> getOpenPositions(String symbol) {
		return openPositions.get(symbol);
	}

	public List<Execution> getExecutions(String symbol) {
		return executions.get(symbol);
	}
}
