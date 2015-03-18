package com.fdt.lts.client.obj;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
		private final ReentrantLock lock = new ReentrantLock();

		private String id;
		private String symbol;
		private double qty;
		private double PnL;
		private Date created;
		private double acPnL;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			try {
				lock.lock();
				this.id = id;
			} finally {
				lock.unlock();
			}
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			try {
				lock.lock();
				this.symbol = symbol;
			} finally {
				lock.unlock();
			}

		}

		public double getQty() {
			return qty;
		}

		public void setQty(double qty) {
			try {
				lock.lock();
				this.qty = qty;
			} finally {
				lock.unlock();
			}

		}

		public double getPnL() {
			return PnL;
		}

		public void setPnL(double pnL) {
			try {
				lock.lock();
				PnL = pnL;
			} finally {
				lock.unlock();
			}

		}

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			try {
				lock.lock();
				this.created = created;
			} finally {
				lock.unlock();
			}

		}

		public double getAcPnL() {
			return acPnL;
		}

		public void setAcPnL(double acPnL) {
			try {
				lock.lock();
				this.acPnL = acPnL;
			} finally {
				lock.unlock();
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
		private final ReentrantLock lock = new ReentrantLock();

		private double price;

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			try {
				lock.lock();
				this.price = price;
			} finally {
				lock.unlock();
			}
		}
	}

	public class Account {
		// for thread safe
		private final ReentrantLock lock = new ReentrantLock();

		private double dailyPnL;		
		private double PnL;
		private double urPnL;
		private double allTimePnL;
		private String currency;
		private double cash;
		private double margin;
		private double value;

		public double getDailyPnL() {
			return dailyPnL;
		}

		public void setDailyPnL(double dailyPnL) {
			try {
				lock.lock();
				this.dailyPnL = dailyPnL;
			} finally {
				lock.unlock();
			}
		}
		
		public double getMargin() {
			return margin;
		}

		public void setMargin(double margin) {
			try {
				lock.lock();
				this.margin = margin;
			} finally {
				lock.unlock();
			}
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			try {
				lock.lock();
				this.value = value;
			} finally {
				lock.unlock();
			}
		}

		public double getPnL() {
			return PnL;
		}

		public void setPnL(double pnL) {
			try {
				lock.lock();
				PnL = pnL;
			} finally {
				lock.unlock();
			}
		}

		public double getUrPnL() {
			return urPnL;
		}

		public void setUrPnL(double urPnL) {
			try {
				lock.lock();
				this.urPnL = urPnL;
			} finally {
				lock.unlock();
			}
		}

		public double getAllTimePnL() {
			return allTimePnL;
		}

		public void setAllTimePnL(double allTimePnL) {
			try {
				lock.lock();
				this.allTimePnL = allTimePnL;
			} finally {
				lock.unlock();
			}
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			try {
				lock.lock();
				this.currency = currency;
			} finally {
				lock.unlock();
			}
		}

		public double getCash() {
			return cash;
		}

		public void setCash(double cash) {
			try {
				lock.lock();
				this.cash = cash;
			} finally {
				lock.unlock();
			}
		}
	}

	public class Execution { // need to check the fields
		// for thread safe
		private final ReentrantLock lock = new ReentrantLock();

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

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			try {
				lock.lock();
				this.created = created;
			} finally {
				lock.unlock();
			}

		}

		public Date getModified() {
			return modified;
		}

		public void setModified(Date modified) {
			try {
				lock.lock();
				this.modified = modified;
			} finally {
				lock.unlock();
			}

		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			try {
				lock.lock();
				this.id = id;
			} finally {
				lock.unlock();
			}

		}

		public String getServerID() {
			return serverID;
		}

		public void setServerID(String serverID) {
			try {
				lock.lock();
				this.serverID = serverID;
			} finally {
				lock.unlock();
			}

		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			try {
				lock.lock();
				this.symbol = symbol;
			} finally {
				lock.unlock();
			}

		}

		public String getSide() {
			return side;
		}

		public void setSide(String side) {
			try {
				lock.lock();
				this.side = side;
			} finally {
				lock.unlock();
			}

		}

		public long getQuantity() {
			return quantity;
		}

		public void setQuantity(long quantity) {
			try {
				lock.lock();
				this.quantity = quantity;
			} finally {
				lock.unlock();
			}

		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			try {
				lock.lock();
				this.price = price;
			} finally {
				lock.unlock();
			}

		}

		public String getOrderID() {
			return orderID;
		}

		public void setOrderID(String orderID) {
			try {
				lock.lock();
				this.orderID = orderID;
			} finally {
				lock.unlock();
			}

		}

		public String getParentOrderID() {
			return parentOrderID;
		}

		public void setParentOrderID(String parentOrderID) {
			try {
				lock.lock();
				this.parentOrderID = parentOrderID;
			} finally {
				lock.unlock();
			}

		}

		public String getStrategyID() {
			return strategyID;
		}

		public void setStrategyID(String strategyID) {
			try {
				lock.lock();
				this.strategyID = strategyID;
			} finally {
				lock.unlock();
			}

		}

		public String getExecID() {
			return execID;
		}

		public void setExecID(String execID) {
			try {
				lock.lock();
				this.execID = execID;
			} finally {
				lock.unlock();
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
