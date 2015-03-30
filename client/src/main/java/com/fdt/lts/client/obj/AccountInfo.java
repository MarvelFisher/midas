package com.fdt.lts.client.obj;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountInfo {
	private Account account;
	// private List<ClosePosition> closePositions;
	private ConcurrentHashMap<String, List<OpenPosition>> openPositions;
	// private List<OpenPosition> openPositions;
	private ConcurrentHashMap<String, List<Execution>> executions;

	public Account newAccount() {
		account = new Account();
		return account;
	}

	public void addOpenPosition(String symbol, String orderID,
			OpenPosition oposition) {
		if (openPositions == null)
			openPositions = new ConcurrentHashMap<String, List<OpenPosition>>();
		List<OpenPosition> opList = openPositions.get(symbol);
		if (opList == null) {
			opList = new ArrayList<OpenPosition>();
			openPositions.put(symbol, opList);
		}
		for (OpenPosition position : opList) {
			if (position.getId().equals(orderID))
				opList.remove(orderID);
		}
		opList.add(oposition);
	}

	public void addExecution(String symbol, String orderID, Execution e) {
		if (executions == null)
			executions = new ConcurrentHashMap<String, List<Execution>>();
		List<Execution> exeList = executions.get(symbol);
		if (exeList == null) {
			exeList = new ArrayList<Execution>();
			executions.put(symbol, exeList);
		}
		for (Execution exe : exeList) {
			if (exe.getId().equals(orderID))
				exeList.remove(orderID);
		}
		exeList.add(e);
	}

	public class Position {
		// for thread safe
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		private String id;
		private String account;
		private String user;
		private String symbol;
		private double qty;
		private double PnL;
		private Date created;
		private double acPnL;

		public String getId() {
			try {
				lock.readLock().lock();
				return id;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setId(String id) {
			try {
				lock.writeLock().lock();
				this.id = id;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public String getAccount() {
			try {
				lock.readLock().lock();
				return account;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setAccount(String account) {
			try {
				lock.writeLock().lock();
				this.account = account;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getUser() {
			try {
				lock.readLock().lock();
				return user;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setUser(String user) {
			try {
				lock.writeLock().lock();
				this.user = user;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getSymbol() {
			try {
				lock.readLock().lock();
				return symbol;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setSymbol(String symbol) {
			try {
				lock.writeLock().lock();
				this.symbol = symbol;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public double getQty() {
			try {
				lock.readLock().lock();
				return qty;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setQty(double qty) {
			try {
				lock.writeLock().lock();
				this.qty = qty;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public double getPnL() {
			try {
				lock.readLock().lock();
				return PnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setPnL(double pnL) {
			try {
				lock.writeLock().lock();
				PnL = pnL;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public Date getCreated() {
			try {
				lock.readLock().lock();
				return created;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setCreated(Date created) {
			try {
				lock.writeLock().lock();
				this.created = created;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public double getAcPnL() {
			try {
				lock.readLock().lock();
				return acPnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setAcPnL(double acPnL) {
			try {
				lock.writeLock().lock();
				this.acPnL = acPnL;
			} finally {
				lock.writeLock().unlock();
			}

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
		// for thread safe
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		private double price;
		private double margin;

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			try {
				lock.writeLock().lock();
				this.price = price;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getMargin() {
			try {
				lock.readLock().lock();
				return margin;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setMargin(double margin) {
			try {
				lock.writeLock().lock();
				this.margin = margin;
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public class Account {
		// for thread safe
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		private double dailyPnL;
		private double PnL;
		private double urPnL;
		private double allTimePnL;
		private String currency;
		private double cash;
		private double margin;
		private double value;

		public double getDailyPnL() {
			try {
				lock.readLock().lock();
				return dailyPnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setDailyPnL(double dailyPnL) {
			try {
				lock.writeLock().lock();
				this.dailyPnL = dailyPnL;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getMargin() {
			try {
				lock.readLock().lock();
				return margin;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setMargin(double margin) {
			try {
				lock.writeLock().lock();
				this.margin = margin;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getValue() {
			try {
				lock.readLock().lock();
				return value;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setValue(double value) {
			try {
				lock.writeLock().lock();
				this.value = value;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getPnL() {
			try {
				lock.readLock().lock();
				return PnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setPnL(double pnL) {
			try {
				lock.writeLock().lock();
				PnL = pnL;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getUrPnL() {
			try {
				lock.readLock().lock();
				return urPnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setUrPnL(double urPnL) {
			try {
				lock.writeLock().lock();
				this.urPnL = urPnL;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getAllTimePnL() {
			try {
				lock.readLock().lock();
				return allTimePnL;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setAllTimePnL(double allTimePnL) {
			try {
				lock.writeLock().lock();
				this.allTimePnL = allTimePnL;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public String getCurrency() {
			try {
				lock.readLock().lock();
				return currency;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setCurrency(String currency) {
			try {
				lock.writeLock().lock();
				this.currency = currency;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public double getCash() {
			try {
				lock.readLock().lock();
				return cash;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setCash(double cash) {
			try {
				lock.writeLock().lock();
				this.cash = cash;
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public class Execution { // need to check the fields
		// for thread safe
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
			try {
				lock.readLock().lock();
				return created;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setCreated(Date created) {
			try {
				lock.writeLock().lock();
				this.created = created;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public Date getModified() {
			try {
				lock.readLock().lock();
				return modified;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setModified(Date modified) {
			try {
				lock.writeLock().lock();
				this.modified = modified;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getId() {
			try {
				lock.readLock().lock();
				return id;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setId(String id) {
			try {
				lock.writeLock().lock();
				this.id = id;
			} finally {
				lock.writeLock().unlock();
			}
		}

		public String getServerID() {
			try {
				lock.readLock().lock();
				return serverID;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setServerID(String serverID) {
			try {
				lock.writeLock().lock();
				this.serverID = serverID;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getSymbol() {
			try {
				lock.readLock().lock();
				return symbol;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setSymbol(String symbol) {
			try {
				lock.writeLock().lock();
				this.symbol = symbol;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getSide() {
			try {
				lock.readLock().lock();
				return side;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setSide(String side) {
			try {
				lock.writeLock().lock();
				this.side = side;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public long getQuantity() {
			try {
				lock.readLock().lock();
				return quantity;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setQuantity(long quantity) {
			try {
				lock.writeLock().lock();
				this.quantity = quantity;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public double getPrice() {
			try {
				lock.readLock().lock();
				return price;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setPrice(double price) {
			try {
				lock.writeLock().lock();
				this.price = price;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getOrderID() {
			try {
				lock.readLock().lock();
				return orderID;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setOrderID(String orderID) {
			try {
				lock.writeLock().lock();
				this.orderID = orderID;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getParentOrderID() {
			try {
				lock.readLock().lock();
				return parentOrderID;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setParentOrderID(String parentOrderID) {
			try {
				lock.writeLock().lock();
				this.parentOrderID = parentOrderID;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getStrategyID() {
			try {
				lock.readLock().lock();
				return strategyID;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setStrategyID(String strategyID) {
			try {
				lock.writeLock().lock();
				this.strategyID = strategyID;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getExecID() {
			try {
				lock.readLock().lock();
				return execID;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setExecID(String execID) {
			try {
				lock.writeLock().lock();
				this.execID = execID;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getUser() {
			try {
				lock.readLock().lock();
				return user;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setUser(String user) {
			try {
				lock.writeLock().lock();
				this.user = user;
			} finally {
				lock.writeLock().unlock();
			}

		}

		public String getAccount() {
			try {
				lock.readLock().lock();
				return account;
			} finally {
				lock.readLock().unlock();
			}
		}

		public void setAccount(String account) {
			try {
				lock.writeLock().lock();
				this.account = account;
			} finally {
				lock.writeLock().unlock();
			}
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

