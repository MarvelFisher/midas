package com.cyanspring.apievent.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountInfo {
	private Account account;
	// private List<ClosePosition> closePositions;
	private ConcurrentHashMap<String, OpenPosition> openPositions;
	// private List<OpenPosition> openPositions;
	private ConcurrentHashMap<String, List<Execution>> executions;

	public Account newAccount() {
		account = new Account();
		return account;
	}

	public void addOpenPosition(String symbol, String orderID,
			OpenPosition oposition) {
		if (openPositions == null)
			openPositions = new ConcurrentHashMap<String, OpenPosition>();
		if (openPositions.get(symbol) != null)
			openPositions.remove(symbol);
		openPositions.put(symbol, oposition);
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

	public Account getAccount() {
		return account;
	}

	// public List<ClosePosition> getClosePositions() {
	// return closePositions;
	// }

	public List<OpenPosition> getOpenPositions() {
		if (openPositions == null)
			openPositions = new ConcurrentHashMap<String, OpenPosition>();
		return new ArrayList<OpenPosition>(openPositions.values());
	}

	public List<Execution> getExecutions(String symbol) {
		if (executions == null)
			executions = new ConcurrentHashMap<String, List<Execution>>();
		return executions.get(symbol);
	}
}

